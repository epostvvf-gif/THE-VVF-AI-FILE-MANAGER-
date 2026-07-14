import re

with open('.github/workflows/android.yml', 'r') as f:
    content = f.read()

setup_gradle_regex = r"(uses: gradle/actions/setup-gradle@v4\n\s+with:\n\s+gradle-version: '9\.3\.1'(?:\n\s+cache-read-only:.*)?\n)"
fix_script = """
    - name: Fix line endings and grant execute permission to gradlew
      run: |
        sed -i 's/\\r$//' gradlew
        chmod +x gradlew || true
"""

content = re.sub(setup_gradle_regex, r"\1" + fix_script, content)

with open('.github/workflows/android.yml', 'w') as f:
    f.write(content)
