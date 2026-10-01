import os

base_path = r"c:\Users\Anurag_Bhushan\Desktop\LifeLinkAI\backend\src\main\java\com\lifelinkai\backend"
packages = ['model', 'dto', 'repository', 'service', 'controller']

# Common code generator
def create_file(subpath, content):
    full_path = os.path.join(base_path, subpath)
    os.makedirs(os.path.dirname(full_path), exist_ok=True)
    with open(full_path, 'w', encoding='utf-8') as f:
        f.write(content.strip() + '\n')

def gen_enums():
    create_file('model/Availability.java', '''package com.lifelinkai.backend.model;
public enum Availability { AVAILABLE, UNAVAILABLE, ON_LEAVE }''')
    create_file('model/OperationalStatus.java', '''package com.lifelinkai.backend.model;
public enum OperationalStatus { ACTIVE, INACTIVE, MAINTENANCE }''')
    create_file('model/AmbulanceType.java', '''package com.lifelinkai.backend.model;
public enum AmbulanceType { BASIC, ADVANCED, ICU }''')
    create_file('model/AmbulanceStatus.java', '''package com.lifelinkai.backend.model;
public enum AmbulanceStatus { IDLE, ASSIGNED, IN_TRANSIT, MAINTENANCE }''')

gen_enums()
