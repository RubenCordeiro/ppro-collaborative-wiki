{% set user = salt['pillar.get']('wechangers:user') %}

wechangers_user:
  user:
    - present
    - name: {{ user }}
    - remove_groups: False
    - groups:
      - sudo

wechangers_user_sudo:
  file:
    - append
    - name: /etc/sudoers.d/{{ user }}
    - text: "{{ user }} ALL=(ALL) NOPASSWD:ALL"
