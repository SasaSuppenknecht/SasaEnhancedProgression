from pathlib import Path
import sys

# Set the directory you want to search
TARGET_DIR = Path(r"H:\Sascha\Programmierstuff\IntelliJ IDEA\Minecraft Plugins\1.21\SasaEnhancedProgression\1.21.8\data\minecraft\recipe")

def main():
    if not TARGET_DIR.exists() or not TARGET_DIR.is_dir():
        print(f"Directory not found: {TARGET_DIR}", file=sys.stderr)
        return

    while True:
        term = input("Enter search term (or type 'exit' to quit): ").strip()
        if term.lower() == "exit":
            print("Exiting program.")
            break

        # case-insensitive match; if term is empty, show all filenames (without extension)
        term_lower = term.lower()

        print("Matching file names:")
        for p in sorted(TARGET_DIR.iterdir()):
            if not p.is_file():
                continue
            name = p.stem  # filename without extension
            if term_lower == "" or term_lower in name.lower():
                print(f'"minecraft:{name}"', end=",\n")
        print()  # Add a blank line for better readability

if __name__ == "__main__":
    main()