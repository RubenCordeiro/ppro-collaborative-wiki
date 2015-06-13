# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  # All Vagrant configuration is done here.

  config.vm.box = "ubuntu/trusty64"

  config.vm.provider "virtualbox" do |v|
    v.memory = 4096
    v.cpus = 4
  end

  {
    4000 => 4000, # rails
    5432 => 65431, # postgres
    9292 => 9090, #weaki_default
    8080 => 8080, 
    8081 => 8081
  }.each do |guest, host|
    config.vm.network "forwarded_port",
      guest: guest,
      host: host,
      auto_correct: true
  end

  config.ssh.forward_agent = true

  config.vm.synced_folder "salt/roots/", "/srv/"

  $script = <<-SCRIPT
    curl -s https://bintray.com/user/downloadSubjectPublicKey?username=sbt | sudo apt-key add -
    echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
    curl -sL https://deb.nodesource.com/setup_0.12 | sudo bash -
    sudo apt-get update
    sudo apt-get install --force-yes -y sbt
    sudo apt-get install bundler libicu-dev libz-dev libsqlite3-dev git nodejs -y
    cd /vagrant
    bundle install
    echo "\n\ncd /vagrant" >> /home/vagrant/.bashrc
  SCRIPT

  config.vm.provision "shell", inline: $script

  config.vm.provision :salt do |salt|
    salt.install_type = "git"
    salt.install_args = "v2014.1.13"
    salt.minion_config = "salt/minion-vagrant"
    salt.run_highstate = true
    salt.colorize = true
    salt.verbose = true
    salt.log_level = "info"
  end

  #config.vm.provision :shell, inline: <<-SCRIPT
#    if ! grep -q "weaki config" /home/vagrant/.bashrc; then
#      cat /vagrant/.bashrc-vagrant >> /home/vagrant/.bashrc
#    fi
#  SCRIPT

  VAGRANTFILE_LOCAL = "Vagrantfile.local"
  eval File.read VAGRANTFILE_LOCAL if File.exists? VAGRANTFILE_LOCAL
end