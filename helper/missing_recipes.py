import re
import os

TARGET_DIR = r"C:\Users\pramb\Documents\GitHub\SasaEnhancedProgression\src\main\resources\techtreedatapack\data\techtree\advancement"
RECIPES = r"C:\Users\pramb\Documents\GitHub\SasaEnhancedProgression\data\minecraft\recipe"

def search_file(file_name, pattern):
    pass


def main():
    missing_recipes = []
    for root, _, files in os.walk(RECIPES):
        for file in files:
            missing_recipes.append(file.removesuffix(".json"))
    
    pattern = re.compile("\"minecraft:([a-z_]+)\"")
    for root, _, files in os.walk(TARGET_DIR):
        #print(files)
        for file in files:
            if file.endswith(".json"):
                file_path = os.path.join(root, file)
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                    matches = pattern.findall(content)
                    missing_recipes = [r for r in missing_recipes if r not in matches]
    print("Missing recipes:")
    print("\n".join(sorted(set(missing_recipes))))
    print(f"Total missing recipes: {len(missing_recipes)}")


if __name__ == "__main__":
    main()