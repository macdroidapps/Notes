#!/usr/bin/env python3
"""
Documentation Search for Claude Code CLI
Searches through indexed documentation and provides context
"""

import json
import sys
from pathlib import Path
from typing import List, Dict, Any
import re


PROJECT_ROOT = Path(__file__).parent.parent
INDEX_FILE = PROJECT_ROOT / ".claude" / "indexed_docs.json"


class DocumentSearcher:
    """Search through indexed documentation"""

    def __init__(self, index_path: Path):
        with open(index_path, 'r', encoding='utf-8') as f:
            self.index = json.load(f)

        self.chunks = self.index["chunks"]
        self.keyword_index = self.index.get("keyword_index", {})

    def search(self, query: str, max_results: int = 5) -> List[Dict[str, Any]]:
        """Search for relevant documentation chunks"""
        query_lower = query.lower()
        results = []

        # Score each chunk
        for chunk in self.chunks:
            score = self._calculate_relevance(chunk, query_lower)
            if score > 0:
                results.append({
                    "chunk": chunk,
                    "score": score
                })

        # Sort by score and return top results
        results.sort(key=lambda x: x["score"], reverse=True)
        return results[:max_results]

    def _calculate_relevance(self, chunk: Dict[str, Any], query: str) -> float:
        """Calculate relevance score for a chunk"""
        content = chunk["content"].lower()
        score = 0.0

        # Exact phrase match (highest score)
        if query in content:
            score += 10.0

        # Keyword matching
        query_words = re.findall(r'\w+', query)
        for word in query_words:
            if len(word) < 3:  # Skip short words
                continue

            # Count occurrences
            occurrences = content.count(word)
            score += occurrences * 2.0

        # Boost if keywords match
        chunk_keywords = [k.lower() for k in chunk.get("keywords", [])]
        for word in query_words:
            if word in chunk_keywords:
                score += 5.0

        # Boost by section title relevance
        section_title = chunk["metadata"].get("section_title", "").lower()
        for word in query_words:
            if word in section_title:
                score += 3.0

        return score

    def search_by_command(self, command: str) -> List[Dict[str, Any]]:
        """Search based on /help command"""
        command = command.strip().lower()

        # Map commands to search queries
        command_map = {
            "/help": "help commands available",
            "/help architecture": "clean architecture layers domain presentation data",
            "/help feature": "add new feature use case repository viewmodel",
            "/help koin": "koin dependency injection module",
            "/help sqldelight": "sqldelight database queries schema",
            "/help compose": "compose multiplatform ui screen",
            "/help testing": "testing unit test",
            "/help git": "git workflow branch"
        }

        query = command_map.get(command, command.replace("/help ", ""))
        return self.search(query, max_results=3)

    def format_results(self, results: List[Dict[str, Any]]) -> str:
        """Format search results for display"""
        if not results:
            return "❌ No relevant documentation found."

        output = []
        output.append("📚 **Found relevant documentation:**\n")

        for i, result in enumerate(results, 1):
            chunk = result["chunk"]
            score = result["score"]

            source = chunk["source"]
            section = chunk["metadata"].get("section_title", "Unknown")
            content = chunk["content"][:300]  # First 300 chars

            output.append(f"**{i}. {source}** - {section}")
            output.append(f"   Relevance: {score:.1f}")
            output.append(f"```")
            output.append(content + "...")
            output.append(f"```\n")

        return "\n".join(output)


def main():
    """Main search function"""
    if not INDEX_FILE.exists():
        print("❌ Index not found. Run: python3 .claude/index_docs.py")
        sys.exit(1)

    searcher = DocumentSearcher(INDEX_FILE)

    # Get query from command line
    if len(sys.argv) < 2:
        print("Usage: python3 .claude/search_docs.py <query>")
        print("   or: python3 .claude/search_docs.py '/help <topic>'")
        sys.exit(1)

    query = " ".join(sys.argv[1:])

    # Check if it's a command
    if query.startswith("/help"):
        results = searcher.search_by_command(query)
    else:
        results = searcher.search(query)

    # Print results
    print(searcher.format_results(results))


if __name__ == "__main__":
    main()

