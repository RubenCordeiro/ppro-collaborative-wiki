{% set postgresql = salt['pillar.get']('wechangers:postgresql') %}

postgresql.conf:
  file:
    - append
    - name: /etc/postgresql/9.3/main/postgresql.conf
    - source: salt://wechangers/postgresql/files/postgresql.{{ postgresql['env'] }}.conf
    - require:
      - pkg: postgresql
