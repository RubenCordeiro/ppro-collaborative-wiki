include:
  - wechangers.user

gpg-import-D39DC0E3:
  cmd.run:
    - user: {{ salt['pillar.get']('wechangers:user') }}
    - require:
      - user: wechangers_user
    - name: gpg --keyserver hkp://keys.gnupg.net --recv-keys D39DC0E3
    - unless: gpg --fingerprint |fgrep 'Key fingerprint = 409B 6B17 96C2 7546 2A17  0311 3804 BB82 D39D C0E3'

wechangers_ruby:
  rvm:
    - installed
    - name: ruby-2.1.1
    - default: True
    - user: {{ salt['pillar.get']('wechangers:user') }}
    - require:
      - pkg: wechangers_ruby_deps
      - cmd: gpg-import-D39DC0E3
      - user: wechangers_user

wechangers_ruby_deps:
  pkg.installed:
    - names:
      - build-essential
      - openssl
      - libssl-dev
      - libreadline6
      - libreadline6-dev
      - zlib1g
      - zlib1g-dev
      - sqlite3
      - libsqlite3-0
      - libsqlite3-dev
      - libyaml-dev
      - libc6-dev
      - libncurses5-dev
      - autoconf
      - automake
      - libtool
      - bison
      - git-core
      - libpq-dev    # postgresql
      - nodejs       # javascript
