var realtimeweaki = {};

realtimeweaki.messages = {};

realtimeweaki.messages.Register = function (name) {
    return JSON.stringify({
        mType : "Control",
        msg : "Register",
        name: name
    });
};

realtimeweaki.messages.Say = function (content) {
    return JSON.stringify({
        mType : "Control",
        msg : "Say",
        content: content
    });
};

realtimeweaki.baseCharUri = "ws://localhost:8081/chat";

realtimeweaki.connectToChat = function (document, username, messageReceived) {
    messageReceived = messageReceived || console.log;

    var wsUri = realtimeweaki.baseCharUri + "/" + document;
    var wSocket = new WebSocket(wsUri);

    var connectedClient = function (ev) {
        var msg = JSON.parse(ev.data);

        if (msg.mType != "Control")
            return;

        switch (msg.msg) {
            case "Join":
                if (msg.name == username) {
                    messageReceived(msg.name + " joined");
                    wSocket.onmessage = registeredClient;
                }
                break;
        }
    };

    var registeredClient = function (ev) {
        var msg = JSON.parse(ev.data);

        if (msg.mType == "Control") {
            switch (msg.msg) {
                case "Join":
                    messageReceived(msg.name + " joined");
                    break;
                case "Left":
                    messageReceived(msg.name + " left");
                    break;

            }
        } else if (msg.mType == "Chat") {
            switch (msg.msg) {
                case "Said":
                    messageReceived(msg.name + " said: " + msg.content);
                    break;
            }
        } else {
            console.error("Unrecognized message type: '" + msg.mType + " 'in '" + ev.data);
        }
    };

    wSocket.onopen = function (ev) {
        wSocket.onmessage = connectedClient;
        wSocket.send(realtimeweaki.messages.Register(username));
    };

    wSocket.onerror = function (ev) { console.error("ERROR: " + ev.data); };
    wSocket.onclose = function (ev) { console.log("CLOSED"); };

    return {
        send: function (messageContent) { wSocket.send(realtimeweaki.messages.Say(messageContent)); }
    };
};