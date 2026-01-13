#!/usr/bin/env python3
"""
Claude Code CLI Helper with RAG
Provides context-aware assistance using indexed documentation
"""

import json
import sys
import subprocess
from pathlib import Path
from typing import List, Dict, Any


PROJECT_ROOT = Path(__file__).parent.parent
INDEX_FILE = PROJECT_ROOT / ".claude" / "indexed_docs.json"
CONTEXT_FILE = PROJECT_ROOT / ".claude" / "project_context.md"


def get_git_context() -> Dict[str, Any]:
    """Get current git context"""
    try:
        # Current branch
        branch = subprocess.check_output(
            ["git", "branch", "--show-current"],
            cwd=PROJECT_ROOT,
            text=True
        ).strip()

        # Modified files
        status = subprocess.check_output(
            ["git", "status", "--short"],
            cwd=PROJECT_ROOT,
            text=True
        ).strip()

        modified_files = [line[3:] for line in status.split('\n') if line] if status else []

        # Recent commits
        log = subprocess.check_output(
            ["git", "log", "--oneline", "-5"],
            cwd=PROJECT_ROOT,
            text=True
        ).strip()

        recent_commits = log.split('\n') if log else []

        return {
            "branch": branch,
            "modified_files": modified_files,
            "recent_commits": recent_commits
        }
    except Exception as e:
        return {
            "branch": "unknown",
            "modified_files": [],
            "recent_commits": [],
            "error": str(e)
        }


def search_documentation(query: str, max_results: int = 3) -> List[Dict[str, Any]]:
    """Search indexed documentation"""
    if not INDEX_FILE.exists():
        return []

    with open(INDEX_FILE, 'r', encoding='utf-8') as f:
        index = json.load(f)

    chunks = index["chunks"]
    query_lower = query.lower()
    results = []

    for chunk in chunks:
        content_lower = chunk["content"].lower()
        score = 0.0

        # Simple scoring
        if query_lower in content_lower:
            score += 10.0

        for word in query_lower.split():
            if len(word) > 2:
                score += content_lower.count(word) * 2.0

        if score > 0:
            results.append({"chunk": chunk, "score": score})

    results.sort(key=lambda x: x["score"], reverse=True)
    return results[:max_results]


def build_context(query: str) -> str:
    """Build context for Claude"""
    context_parts = []

    # 1. Project Context
    if CONTEXT_FILE.exists():
        with open(CONTEXT_FILE, 'r', encoding='utf-8') as f:
            context_parts.append("# Project Context\n")
            context_parts.append(f.read())

    # 2. Git Context
    context_parts.append("\n# Git Context\n")
    git_ctx = get_git_context()
    context_parts.append(f"**Branch:** {git_ctx['branch']}\n")

    if git_ctx['modified_files']:
        context_parts.append("**Modified files:**\n")
        for file in git_ctx['modified_files'][:10]:  # Limit to 10
            context_parts.append(f"- {file}\n")

    if git_ctx['recent_commits']:
        context_parts.append("\n**Recent commits:**\n")
        for commit in git_ctx['recent_commits'][:3]:
            context_parts.append(f"- {commit}\n")

    # 3. Relevant Documentation
    docs = search_documentation(query)
    if docs:
        context_parts.append("\n# Relevant Documentation\n")
        for i, doc in enumerate(docs, 1):
            chunk = doc["chunk"]
            source = chunk["source"]
            section = chunk["metadata"].get("section_title", "")
            content = chunk["content"][:500]  # Limit content

            context_parts.append(f"\n## From {source} - {section}\n")
            context_parts.append(f"{content}...\n")

    return "".join(context_parts)


def format_help_response(topic: str) -> str:
    """Format response for /help commands"""
    help_topics = {
        "architecture": "Clean Architecture with Feature Slicing",
        "koin": "Dependency Injection with Koin",
        "compose": "Compose Multiplatform UI",
        "sqldelight": "SQLDelight Database",
        "testing": "Testing Patterns",
        "git": "Git Workflow"
    }

    if not topic or topic == "help":
        response = ["# Available Help Topics\n"]
        for key, desc in help_topics.items():
            response.append(f"- `/help {key}` - {desc}")
        return "\n".join(response)

    # Search for specific topic
    docs = search_documentation(topic)
    if not docs:
        return f"❌ No documentation found for: {topic}"

    response = [f"# Help: {topic.title()}\n"]
    for doc in docs:
        chunk = doc["chunk"]
        response.append(f"\n## {chunk['source']}\n")
        response.append(chunk["content"][:600])
        response.append("\n---\n")

    return "\n".join(response)


def main():
    """Main CLI function"""
    if len(sys.argv) < 2:
        print("Usage:")
        print("  python3 .claude/claude_helper.py <query>")
        print("  python3 .claude/claude_helper.py /help [topic]")
        print("  python3 .claude/claude_helper.py --context <query>  (get context only)")
        sys.exit(1)

    query = " ".join(sys.argv[1:])

    # Handle --context flag
    if "--context" in sys.argv:
        query = query.replace("--context", "").strip()
        context = build_context(query)
        print(context)
        sys.exit(0)

    # Handle /help commands
    if query.startswith("/help"):
        topic = query.replace("/help", "").strip()
        response = format_help_response(topic)
        print(response)
        sys.exit(0)

    # Build full context
    context = build_context(query)

    # Output for piping to Claude
    print("=" * 80)
    print("CONTEXT FOR CLAUDE")
    print("=" * 80)
    print(context)
    print("\n" + "=" * 80)
    print(f"USER QUERY: {query}")
    print("=" * 80)


if __name__ == "__main__":
    main()

