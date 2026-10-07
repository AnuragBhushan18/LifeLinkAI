import json
import re
import os

transcript_path = r"C:\Users\Anurag_Bhushan\.gemini\antigravity\brain\48dd78d0-06e3-49c5-bd0a-4c2a8ad76a70\.system_generated\logs\transcript_full.jsonl"
project_dir = r"c:\Users\Anurag_Bhushan\Desktop\LifeLinkAI"

with open(transcript_path, 'r', encoding='utf-8') as f:
    for line in f:
        try:
            entry = json.loads(line)
            if 'tool_calls' in entry:
                for tc in entry['tool_calls']:
                    name = tc.get('name') or tc.get('function', {}).get('name')
                    args = tc.get('args') or tc.get('function', {}).get('arguments')
                    if not args: continue
                    if type(args) == str: args = json.loads(args)

                    if 'run_command' in name:
                        cmd = args.get('CommandLine', '')
                        matches = re.finditer(r'@"\n(.*?)\n"@\s*\|\s*Out-File\s+-FilePath\s+([^\s]+)', cmd, re.DOTALL)
                        for match in matches:
                            content = match.group(1).replace(r'\', '\')
                            filepath = match.group(2)
                            full_path = os.path.join(project_dir, filepath.replace('\', '/'))
                            os.makedirs(os.path.dirname(full_path), exist_ok=True)
                            with open(full_path, 'w', encoding='utf-8') as out_f:
                                out_f.write(content)

                    if 'write_to_file' in name:
                        filepath = args.get('TargetFile')
                        content = args.get('CodeContent')
                        if filepath and content:
                            if filepath.startswith('"') and filepath.endswith('"'): filepath = filepath[1:-1]
                            content = content.replace(r'\', '\')
                            os.makedirs(os.path.dirname(filepath), exist_ok=True)
                            with open(filepath, 'w', encoding='utf-8') as out_f:
                                out_f.write(content)

        except Exception as e:
            pass
