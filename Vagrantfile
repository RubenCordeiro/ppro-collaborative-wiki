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
    9292 => 9090 #weaki_default,
  }.each do |guest, host|
    config.vm.network "forwarded_port",
      guest: guest,
      host: host,
      auto_correct: true
  end

  config.ssh.forward_agent = true

  $script = <<-SCRIPT
    echo "deb http://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
    curl -sL https://deb.nodesource.com/setup | sudo bash -
    sudo apt-get update
    sudo apt-get install -y --force-yes sbt nodejs
  
    sudo apt-get install sbt node
    cd /vagrant
    echo "\n\ncd /vagrant" >> /home/vagrant/.bashrc
  SCRIPT

  config.vm.provision "shell", inline: $script

  VAGRANTFILE_LOCAL = "Vagrantfile.local"
  eval File.read VAGRANTFILE_LOCAL if File.exists? VAGRANTFILE_LOCAL
end