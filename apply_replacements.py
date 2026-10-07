import json
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

                    if 'replace_file_content' in name:
                        filepath = args.get('TargetFile')
                        if not filepath: continue
                        if filepath.startswith('"') and filepath.endswith('"'): filepath = filepath[1:-1]
                        
                        target_content = args.get('TargetContent', '')
                        replacement = args.get('ReplacementContent', '')
                        
                        if 'java' in filepath or 'pom.xml' in filepath:
                            target_content = target_content.replace(r'\\', '\\')
                            replacement = replacement.replace(r'\\', '\\')
                            
                            try:
                                with open(filepath, 'r', encoding='utf-8') as source_f:
                                    code = source_f.read()
                                if target_content in code:
                                    code = code.replace(target_content, replacement)
                                    with open(filepath, 'w', encoding='utf-8') as source_f:
                                        source_f.write(code)
                                    print(f"Replaced in {os.path.basename(filepath)}!")
                            except Exception as e:
                                pass

        except Exception as e:
            pass

