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
                    
                    if type(args) == str:
                        args = json.loads(args)

                    if 'run_command' in name:
                        cmd = args.get('CommandLine', '')
                        if cmd.startswith('"') and cmd.endswith('"'): cmd = cmd[1:-1]
                        
                        # Find matches for @"..."@ | Out-File
                        matches = re.finditer(r'@"\n(.*?)\n"@\s*\|\s*Out-File\s+-FilePath\s+([^\s]+)', cmd, re.DOTALL)
                        for match in matches:
                            content = match.group(1).replace('\\', '\')
                            # DO NOT replace \" here. Wait! The json parser already unescaped JSON string \" into ".
                            # But wait, in the PowerShell string, I typed `"getEmergencyCount"`. It wasn't escaped.
                            # Ah, wait! `\"` inside the JSON string became `"` in Python.
                            # So `cmd` actually contains `return "[{\"name\": ...`
                            # Wait, the `cmd` is a python string. `re.finditer` gets it.
                            # Oh, I see! In `AIToolRegistry`, I originally wrote:
                            # `"[{\"name\": \"getEmergencyCount\", ...`
                            # But wait, PowerShell string @"..."@ doesn't need quotes escaped! Wait, it's a Java string inside PowerShell!
                            # So I originally wrote `"[{\"name\": \"getEmergencyCount\" ... `
                            # Let's verify how I originally wrote it.
                            pass

        except Exception as e:
            print("Error parsing:", e)
