include:
  - wechangers.postgresql.conf

postgresql:
  pkg:
    - installed
    - name: postgresql-9.3
  service:
    - running
    - enable: True
    - watch:
      - sls: wechangers.postgresql.conf
    - require:
      - pkg: postgresql
      - sls: wechangers.postgresql.conf

{% set user = salt['pillar.get']('wechangers:user') %}
{% set postgresql = salt['pillar.get']('wechangers:postgresql') %}

wechangers_postgresql:
  postgres_user:
    - present
    - name: {{ user }}
    - password: {{ postgresql['user_password'] }}
    - createdb: {{ postgresql['createdb'] }}
    - require:
      - service: postgresql
  postgres_database:
    - present
    - name: {{ postgresql['database'] }}
    - owner: {{ user }}
    - require:
      - postgres_user: {{ user }}
