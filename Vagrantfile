# -*- mode: ruby -*-
# vi: set ft=ruby :

# All Vagrant configuration is done below. The "2" in Vagrant.configure
# configures the configuration version (we support older styles for
# backwards compatibility). Please don't change it unless you know what
# you're doing.
Vagrant.configure(2) do |config|
  # The most common configuration options are documented and commented below.
  # For a complete reference, please see the online documentation at
  # https://docs.vagrantup.com.

  # Every Vagrant development environment requires a box. You can search for
  # boxes at https://atlas.hashicorp.com/search.
  config.vm.box = "ubuntu/trusty64"

  config.vm.box_check_update = false

  # The url from where the 'config.vm.box' box will be fetched if it
  # doesn't already exist on the user's system.
  # http://www.vagrantbox.es/
  # config.vm.box_url = "http://files.vagrantup.com/trusty64.box"

  if Vagrant.has_plugin?("vagrant-cachier")
      # Configure cached packages to be shared between instances of the same base box.
      # More info on http://fgrehm.viewdocs.io/vagrant-cachier/usage
      config.cache.scope = :box
  end

  config.vm.define "mailchimp"
  # config.vm.define "mailchimp" do |mailchimp|
  # end

  # configurating the vm
  config.vm.provider "virtualbox" do |v|
    v.name = "mailchimp"
    # max 75% CPU cap
    v.customize ["modifyvm", :id, "--cpuexecutioncap", "75"]
    # give vm max 3GB ram
    v.memory = 2048
  end

  # run "vagrant-machine-setup.sh" shell script when setting up our machine
  config.vm.provision :shell, :privileged => false, :path => "vagrant-machine-setup.sh"
  config.vm.provision :shell, :privileged => false, :path => "vagrant-machine-run.sh",run: "always"

  # Create a forwarded port mapping which allows access to a specific port
  # within the machine from a port on the host machine. In the example below,
  # accessing "localhost:8080" will access port 80 on the guest machine.
  config.vm.network :forwarded_port, guest: 9000, host: 9013, id: "play"
  config.vm.network :forwarded_port, guest: 9999, host: 9913, id: "debug"
  config.vm.network :forwarded_port, guest: 22,   host: 2213, id: "ssh"
  config.vm.network :forwarded_port, guest: 27017,   host: 27013, id: "mongodb"

  # Create a private network, which allows host-only access to the machine
  # using a specific IP.
  config.vm.network :private_network, ip: "10.10.10.13"

  # Create a public network, which generally matched to bridged network.
  # Bridged networks make the machine appear as another physical device on
  # your network.
  # config.vm.network :public_network

  # If true, then any SSH connections made will enable agent forwarding.
  # Default value: false
  # config.ssh.forward_agent = true

  # Share an additional folder to the guest VM. The first argument is
  # the path on the host to the actual folder. The second argument is
  # the path on the guest to mount the folder. And the optional third
  # argument is a set of non-required options.
  # config.vm.synced_folder "../source/xola-mailchimp", "/mailchimp", create: true

end
