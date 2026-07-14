with open('.github/workflows/android.yml', 'r') as f:
    lines = f.readlines()

with open('.github/workflows/android.yml', 'w') as f:
    for line in lines:
        if "sed -i 's/" in line:
            f.write("        sed -i 's/\\r$//' gradlew\n")
        else:
            f.write(line)
