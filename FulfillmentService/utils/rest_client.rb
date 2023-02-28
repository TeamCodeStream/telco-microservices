
require 'json'
require "net/http"
require "uri"

class RestClient
  def self.post(url, body, headers)
    uri = URI.parse(url)

    http = Net::HTTP.new(uri.host, uri.port)

    if uri.scheme == 'https'
      http.use_ssl = true
    end

    post = Net::HTTP::Post.new(uri.request_uri)

    post['Content-Type'] = 'application/json'
    headers.each {|k,v| post[k] = v }

    post.body = body.to_json
    response = http.request(post)

    code = response.code.to_s
		if code != "200" && code != "201"
			raise StandardError.new("HTTP response: #{code} - #{response.message}")
    end

    return nil
  end
end
