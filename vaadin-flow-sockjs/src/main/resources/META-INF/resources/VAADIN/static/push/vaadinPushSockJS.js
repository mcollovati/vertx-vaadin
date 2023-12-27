window.vaadinPush = window.vaadinPush || {};
(function(define) {

  @@sockjs-client.contents@@

  var SockJSImpl = window.SockJS;
  var SockJSWrapper = function(url, options) {
    var self = this;

    options = options || {};
    self.reconnectTimerID = null;
    self.reconnectEnabled = false;
    self.reconnectAttempts = 0;
    self.firstMessage = true;
    self.maxReconnectAttempts = options.maxReconnectAttempts || Infinity;
    self.reconnectInterval = options.reconnectInterval || 5000;


    var setupSockJSConnection = function() {
        self.sock = SockJSImpl(url, null, options);
        self.sock.onopen = function() {
            self.firstMessage = true;
            if (self.reconnectTimerID) {
              self.reconnectAttempts = 0;
              // fire separate event for reconnects
              // consistent behavior with adding handlers onopen
              self.onreopen && self.onreopen();
            }
        };

        self.sock.onclose = function (e) {
            if (self.reconnectEnabled) {
              if (self.reconnectAttempts < self.maxReconnectAttempts) {
                self.onreconnect && self.onreconnect();
                self.sock = null;
                // set id so users can cancel
                self.reconnectTimerID = setTimeout(setupSockJSConnection, self.reconnectInterval);
                ++self.reconnectAttempts;
              } else {
                // notify error
                var ev = new Event('reconnectionError');
                ev.transport = self.sock.transport;
                self.onerror && self.onerror(ev);
              }
            } else {
                self.onclose && self.onclose(e);
            }
        };
        self.sock.onmessage = function(e) {
            if (self.firstMessage) {
                self.firstMessage = false;
                // TODO: handle first message?
                self.onopen && self.onopen();
            } else {
                self.onmessage && self.onmessage(e);
            }
        };
        self.sock.onerror = function(e) {
            self.onerror && self.onerror(e);
        }
    };

    setupSockJSConnection();

  };

    SockJSWrapper.prototype.close = function () {
        this.enableReconnect(false);
        this.sock.close();
    };
    SockJSWrapper.prototype.send = function(message) {
        if (this.sock) {
            this.sock.send(message);
        } else {
            throw new Error('SockJS not initialized');
        }
    };
    SockJSWrapper.prototype.enableReconnect = function (enable) {
        var self = this;

        self.reconnectEnabled = enable;
        if (!enable && self.reconnectTimerID) {
          clearTimeout(self.reconnectTimerID);
          self.reconnectTimerID = null;
          self.reconnectAttempts = 0;
        }
    };
    SockJSWrapper.prototype.getTransport = function () {
        return this.sock ? this.sock.transport : null;
    }
    SockJSWrapper.prototype.getReadyState = function () {
        return this.sock ? this.sock.readyState : SockJSImpl.CONNECTING;
    };

  this.SockJS = {
    connect: function(config) {
        var sock = new SockJSWrapper(config.url, config);
        sock.enableReconnect(parseInt(config.reconnectInterval) > 0);
        sock.onopen = config.onOpen;
        sock.onmessage = config.onMessage;
        sock.onclose = config.onClose;
        sock.onerror = config.onError
        sock.onreconnect = config.onReconnect;
        sock.onreopen = config.onReopen;
        return sock;
    },
  }


}).call(window.vaadinPush);
