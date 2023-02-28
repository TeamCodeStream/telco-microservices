# Rubytron

A Ruby-Sinatra app that has a REST API which accepts JSON payloads and performs the required action as outlined in the [*Tron Template](https://newrelic.jiveon.com/docs/DOC-6034) document.


### Install dependencies
Install Bundler version 1.17 for ruby 2.2 support
``` gem install bundler -v 1.17 ```

Run bundler on Gemfile found at the root directory
``` bundle install```


### Start Rubytron

Start the app with
```bundle exec rackup -o 0.0.0.0 -p 7300```


### Upgrading the New Relic Ruby Agent
The new relic agent is obtained by calling ```bundle install``` on the Gemfile file. - ```gem 'newrelic_rpm'```. This pulls the latest version of the agent.

The latest release version can be found at the New Relic's [release notes](https://docs.newrelic.com/docs/release-notes/agent-release-notes/ruby-release-notes) page


# Tron Supported Commands
| Command                 | Support Level                                 |
| ----------------------- | --------------------------------------------- |
| Transaction Name        | **Works**
| Transaction Duration    | **Works**
| Custom Params           | **Works**
| HTTP Response code      | **Works**
| Synchronous HTTP        | **Works**
| Asynchronous HTTP       | **Works**
| DataStore - Mysql       | **Works**
| DataStore - Redis       | *Not Implemented*
| DataStore - RabbitMQ    | *Not Implemented*



# Extending Rubytron APIs
Add a new route in a separate file using Sinatra's New Routes module
```Ruby
require 'sinatra/base'

module Sinatra
  module NewRoutes
    def self.registered(app)
      app.get "/new_route1" do
        return "New Route 1"
      end
      app.get "/new_route2" do
        return "New Route 2"
      end
    end
  end
  register NewRoutes
end
```
Register the route in rubytron.rb & add a require_relative to the new API file
```Ruby
require_relative 'filepath_to_api.rb'

class Rubytron < Sinatra::Base
    register Sinatra::NewRoutes #if ENV['NEWROUTE'] == 'ENABLED'
end
```


## Rubytron and Docker

### Install Docker Machine

    https://docs.docker.com/machine/install-machine/

### Start Docker Machine

    docker-machine start default
    docker-machine env default

Copy the 'export' statements and paste them into your console.

Example:

    export DOCKER_TLS_VERIFY="1"
    export DOCKER_HOST="tcp://192.168.99.100:2376"
    export DOCKER_CERT_PATH="/Users/user/.docker/machine/machines/default"
    export DOCKER_MACHINE_NAME="default"

Note the IP address for the Docker Host (referred to later as 'IP_ADDRESS_OF_DOCKER_HOST').  This IP address can be used to access a running *Tron container via a web browser or curl.

### Build Docker Rubytron Image

Change to the Rubytron directory and run:

    docker build -f Dockerfile -t demotron/rubytron:latest .

### Start Rubytron Contianer

    docker run  -p 7300:7300 -d demotron/rubytron:latest

### Check Rubytron is running

    curl IP_ADDRESS_OF_DOCKER_HOST:7300
