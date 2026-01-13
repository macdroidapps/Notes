#!/usr/bin/env python3
"""
RAG Documentation Indexer for Claude Code CLI
Indexes markdown documentation files for semantic search
"""

import os
import json
import re
from pathlib import Path
from typing import List, Dict, Any
import hashlib

# Configuration
PROJECT_ROOT = Path(__file__).parent.parent
DOCS_DIR = PROJECT_ROOT
OUTPUT_FILE = PROJECT_ROOT / ".claude" / "indexed_docs.json"

# Files to index
DOC_FILES = [
    "README.md",
    "ARCHITECTURE.md",
    "PROJECT_STATUS.md",
    "QUICKSTART.md",
    "AI_HELP_SYSTEM.md",
    "AI_HELP_CHEATSHEET.md",
    "DOCS_NAVIGATION.md",
    "INDEX.md",
    "app/src/main/java/ru/macdroid/subagentstest/kmp-prompt.md"
]


class DocumentChunk:
    """Represents a chunk of documentation"""

    def __init__(self, source: str, content: str, chunk_index: int, metadata: Dict[str, Any]):
        self.source = source
        self.content = content
        self.chunk_index = chunk_index
        self.metadata = metadata
        self.chunk_id = self._generate_id()

    def _generate_id(self) -> str:
        """Generate unique ID for chunk"""
        data = f"{self.source}:{self.chunk_index}:{self.content[:50]}"
        return hashlib.md5(data.encode()).hexdigest()

    def to_dict(self) -> Dict[str, Any]:
        return {
            "id": self.chunk_id,
            "source": self.source,
            "content": self.content,
            "chunk_index": self.chunk_index,
            "metadata": self.metadata,
            "char_count": len(self.content),
            "keywords": self._extract_keywords()
        }

    def _extract_keywords(self) -> List[str]:
        """Extract important keywords from content"""
        # Remove markdown formatting
        text = re.sub(r'[#*`_\[\]()]', '', self.content)

        # Common important terms in the project
        important_terms = [
            'Clean Architecture', 'Koin', 'SQLDelight', 'Compose', 'ViewModel',
            'UseCase', 'Repository', 'Flow', 'StateFlow', 'Coroutines',
            'KMP', 'Multiplatform', 'Android', 'iOS', 'Desktop', 'Web'
        ]

        keywords = []
        text_lower = text.lower()
        for term in important_terms:
            if term.lower() in text_lower:
                keywords.append(term)

        return keywords


def chunk_text(text: str, chunk_size: int = 512, overlap: int = 50) -> List[str]:
    """Split text into overlapping chunks"""
    chunks = []
    start = 0

    while start < len(text):
        end = start + chunk_size
        chunk = text[start:end]

        # Try to break at paragraph or sentence boundary
        if end < len(text):
            last_period = chunk.rfind('.')
            last_newline = chunk.rfind('\n\n')
            break_point = max(last_period, last_newline)

            if break_point > chunk_size * 0.5:  # Only break if not too early
                end = start + break_point + 1
                chunk = text[start:end]

        chunks.append(chunk.strip())
        start = end - overlap if end < len(text) else end

    return chunks


def extract_sections(content: str) -> List[Dict[str, str]]:
    """Extract sections from markdown based on headers"""
    sections = []
    current_section = {"title": "Introduction", "content": "", "level": 0}

    for line in content.split('\n'):
        # Check for markdown headers
        header_match = re.match(r'^(#{1,6})\s+(.+)$', line)

        if header_match:
            # Save previous section
            if current_section["content"].strip():
                sections.append(current_section.copy())

            # Start new section
            level = len(header_match.group(1))
            title = header_match.group(2).strip()
            current_section = {
                "title": title,
                "content": line + "\n",
                "level": level
            }
        else:
            current_section["content"] += line + "\n"

    # Add last section
    if current_section["content"].strip():
        sections.append(current_section)

    return sections


def index_document(file_path: Path) -> List[DocumentChunk]:
    """Index a single markdown document"""
    print(f"📄 Indexing: {file_path.name}")

    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
    except FileNotFoundError:
        print(f"  ⚠️  File not found: {file_path}")
        return []

    chunks = []
    sections = extract_sections(content)

    for section in sections:
        section_content = section["content"]

        # Split section into chunks if it's large
        if len(section_content) > 512:
            text_chunks = chunk_text(section_content, chunk_size=512, overlap=50)
        else:
            text_chunks = [section_content]

        for i, chunk_text_content in enumerate(text_chunks):
            chunk = DocumentChunk(
                source=file_path.name,
                content=chunk_text_content,
                chunk_index=len(chunks),
                metadata={
                    "section_title": section["title"],
                    "section_level": section["level"],
                    "file_path": str(file_path.relative_to(PROJECT_ROOT))
                }
            )
            chunks.append(chunk)

    print(f"  ✅ Created {len(chunks)} chunks")
    return chunks


def create_search_index(chunks: List[DocumentChunk]) -> Dict[str, Any]:
    """Create a searchable index"""
    index = {
        "version": "1.0.0",
        "created_at": "2026-01-12",
        "total_chunks": len(chunks),
        "sources": list(set(chunk.source for chunk in chunks)),
        "chunks": [chunk.to_dict() for chunk in chunks],
        "keyword_index": {}
    }

    # Build keyword index for fast lookup
    for chunk in chunks:
        for keyword in chunk._extract_keywords():
            if keyword not in index["keyword_index"]:
                index["keyword_index"][keyword] = []
            index["keyword_index"][keyword].append(chunk.chunk_id)

    return index


def main():
    """Main indexing function"""
    print("🚀 Starting documentation indexing...\n")

    # Create .claude directory if it doesn't exist
    claude_dir = PROJECT_ROOT / ".claude"
    claude_dir.mkdir(exist_ok=True)

    all_chunks = []

    # Index each document
    for doc_file in DOC_FILES:
        file_path = PROJECT_ROOT / doc_file
        chunks = index_document(file_path)
        all_chunks.extend(chunks)

    # Create search index
    print("\n📊 Creating search index...")
    index = create_search_index(all_chunks)

    # Save to JSON
    print(f"💾 Saving to {OUTPUT_FILE}")
    with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
        json.dump(index, f, indent=2, ensure_ascii=False)

    # Print statistics
    print("\n✅ Indexing complete!")
    print(f"   Total documents: {len(DOC_FILES)}")
    print(f"   Total chunks: {index['total_chunks']}")
    print(f"   Total keywords: {len(index['keyword_index'])}")
    print(f"   Output: {OUTPUT_FILE}")

    # Print keyword statistics
    print("\n🔑 Top Keywords:")
    keyword_counts = {k: len(v) for k, v in index['keyword_index'].items()}
    sorted_keywords = sorted(keyword_counts.items(), key=lambda x: x[1], reverse=True)
    for keyword, count in sorted_keywords[:10]:
        print(f"   {keyword}: {count} chunks")


if __name__ == "__main__":
    main()

