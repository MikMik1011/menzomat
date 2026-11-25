#!/usr/bin/env python3
"""Shows every file of a Menzomat JSON dump as hex and ASCII, like the app's Raw data section.

    read_dump.py menzomat-2026-10-13-2144.json
    cat dump.json | read_dump.py
"""
import json
import sys

BYTES_PER_LINE = 8


def hex_dump(data):
    """A run of identical lines is shown once, then `*`; the last line is always shown."""
    rows = [data[i:i + BYTES_PER_LINE] for i in range(0, len(data), BYTES_PER_LINE)]
    lines = []
    for i, row in enumerate(rows):
        if 0 < i < len(rows) - 1 and row == rows[i - 1]:
            if lines[-1] != "*":
                lines.append("*")
            continue
        text = "".join(chr(b) if 0x20 <= b <= 0x7E else "." for b in row)
        lines.append(f"{i * BYTES_PER_LINE:04X}  {row.hex(' ').upper():<{BYTES_PER_LINE * 3 - 1}}  {text}")
    return "\n".join(lines)


def format_dump(dump):
    # The app leaves out keys that are empty, so only the ids are always there.
    blocks = []
    for app in dump["apps"]:
        title = f"Application 0x{app['aid']:06X}"
        blocks.append(title + (f"\n{app['error']}" if app.get("error") else ""))
        for file in app.get("files", []):
            name = f"File 0x{file['id']:02X}"
            size = f" · {file['size']} B" if file.get("size") is not None else ""
            title = f"{name} · {file.get('type') or '?'}{size}"
            if file.get("dataHex") is not None:
                body = hex_dump(bytes.fromhex(file["dataHex"]))
            else:
                body = f"{name} not read: {file.get('error') or '?'}"
            blocks.append(f"{title}\n{body}".rstrip("\n"))
    return "\n\n".join(blocks)


def main():
    failed = False
    for name in sys.argv[1:] or ["-"]:
        try:
            if name == "-":
                raw = sys.stdin.buffer.read()
            else:
                with open(name, "rb") as f:
                    raw = f.read()
            text = format_dump(json.loads(raw))
        except (OSError, ValueError, KeyError, TypeError, AttributeError) as e:
            print(f"{name}: cannot read this dump ({type(e).__name__}: {e})", file=sys.stderr)
            failed = True
            continue
        if len(sys.argv) > 2:
            print(f"== {name}")
        print(text + "\n")
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
