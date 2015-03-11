var Hapi = require('hapi'),
    Primus = require('primus'),
    Duplex = require('stream').Duplex,
    livedb = require('livedb'),
    sharejs = require('share'),
    backend = livedb.client(livedb.memory()),
    share = require('share').server.createClient({backend: backend});


// Create a server with a host and port
var server = new Hapi.Server();
server.connection({
    host: 'localhost',
    port: 8000
});

var primus = new Primus(server.listener, { transformer: 'browserchannel'} /* options */);
primus.on("connection", function(spark) {
    var stream = new Duplex({objectMode: true});

    stream._read = function() {};
    stream._write = function(chunk, encoding, callback) {
       // if (client.state !== 'closed') {
            spark.write(chunk);
        //}
        callback();
    };

    spark.on('data', function message(data) {
        stream.push(data);
    });

    spark.on('end', function() {
        stream.push(null);
        stream.emit('close');
    });

    stream.on('end', function() {
        spark.end();
    });

    // Give the stream to sharejs
    return share.listen(stream);
});

// Add the route
server.route({
    method: 'GET',
    path:'/hello',
    handler: function (request, reply) {
        reply('hello wiki');
    }
});

// Start the server
server.start();
