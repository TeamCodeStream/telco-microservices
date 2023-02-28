require 'sinatra/base'

module Sinatra
  module UserLoginApi
    def self.registered(app)
      app.get "/login" do
        url = ENV['UserService_URL']
        unless url.nil?
          url += "/check/user/" + rand(1..100).to_s
          response = Typhoeus.get(url, :timeout => 3)
          return "Logged in user! " + response.body
        else
          return "Logged in user! (no user service defined)"
        end
      end
      app.get "/check/user/:id" do
        sleep(rand(0.5..1.5))
        return "Found User #{params[:id]}!"
      end
    end
  end
  register UserLoginApi
end