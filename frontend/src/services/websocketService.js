import { Client } from '@stomp/stompjs';

export const ConnectionState = {
  DISCONNECTED: 'DISCONNECTED',
  CONNECTING: 'CONNECTING',
  CONNECTED: 'CONNECTED',
  RECONNECTING: 'RECONNECTING',
};

class WebSocketService {
  constructor() {
    this.client = null;
    this.connectionState = ConnectionState.DISCONNECTED;
    this.stateListeners = new Set();
    this.reconnectListeners = new Set();
    this.subscriptions = new Map(); // destination -> { stompSub, listeners: Set<cb> }
    this.hasConnectedOnce = false;
  }

  getWsUrl() {
    const customUrl = import.meta.env.VITE_WS_URL;
    if (customUrl) return customUrl;

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const host = window.location.hostname === 'localhost' ? 'localhost:8080' : window.location.host;
    return `${protocol}//${host}/ws`;
  }

  connect() {
    const token = localStorage.getItem('token');
    if (!token) {
      this.updateState(ConnectionState.DISCONNECTED);
      return;
    }

    if (this.client && this.client.active) {
      return;
    }

    const wsUrl = `${this.getWsUrl()}?token=${encodeURIComponent(token)}`;

    this.updateState(this.hasConnectedOnce ? ConnectionState.RECONNECTING : ConnectionState.CONNECTING);

    this.client = new Client({
      brokerURL: wsUrl,
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 3000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      debug: (str) => {
        if (import.meta.env.DEV) {
          // console.debug('[STOMP]', str);
        }
      },
    });

    this.client.onConnect = (frame) => {
      console.log('[STOMP] Connected to broker');
      const wasReconnecting = this.hasConnectedOnce;
      this.hasConnectedOnce = true;
      this.updateState(ConnectionState.CONNECTED);

      // Re-subscribe all active destinations
      for (const [destination, entry] of this.subscriptions.entries()) {
        this.bindStompSubscription(destination, entry);
      }

      // Notify reconnect listeners so components can reconcile missed updates via REST
      if (wasReconnecting) {
        this.reconnectListeners.forEach((listener) => {
          try {
            listener();
          } catch (err) {
            console.error('[STOMP] Reconnect listener error:', err);
          }
        });
      }
    };

    this.client.onDisconnect = () => {
      console.log('[STOMP] Disconnected from broker');
      this.updateState(ConnectionState.DISCONNECTED);
    };

    this.client.onStompError = (frame) => {
      console.error('[STOMP] Broker error:', frame.headers['message'], frame.body);
    };

    this.client.onWebSocketClose = () => {
      if (this.hasConnectedOnce) {
        this.updateState(ConnectionState.RECONNECTING);
      } else {
        this.updateState(ConnectionState.DISCONNECTED);
      }
    };

    try {
      this.client.activate();
    } catch (e) {
      console.error('[STOMP] Activation failed:', e);
      this.updateState(ConnectionState.DISCONNECTED);
    }
  }

  disconnect() {
    if (this.client) {
      try {
        this.client.deactivate();
      } catch (e) {
        console.error('[STOMP] Deactivation error:', e);
      }
      this.client = null;
    }
    this.updateState(ConnectionState.DISCONNECTED);
    this.hasConnectedOnce = false;
  }

  updateState(newState) {
    if (this.connectionState !== newState) {
      this.connectionState = newState;
      this.stateListeners.forEach((listener) => listener(newState));
    }
  }

  onStateChange(listener) {
    this.stateListeners.add(listener);
    listener(this.connectionState);
    return () => this.stateListeners.delete(listener);
  }

  onReconnect(listener) {
    this.reconnectListeners.add(listener);
    return () => this.reconnectListeners.delete(listener);
  }

  subscribe(destination, callback) {
    if (!this.subscriptions.has(destination)) {
      this.subscriptions.set(destination, {
        stompSub: null,
        listeners: new Set(),
      });
    }

    const entry = this.subscriptions.get(destination);
    entry.listeners.add(callback);

    if (this.client && this.client.connected && !entry.stompSub) {
      this.bindStompSubscription(destination, entry);
    }

    // Return unbind function
    return () => {
      entry.listeners.delete(callback);
      if (entry.listeners.size === 0) {
        if (entry.stompSub) {
          try {
            entry.stompSub.unsubscribe();
          } catch (e) {
            // Ignore
          }
        }
        this.subscriptions.delete(destination);
      }
    };
  }

  bindStompSubscription(destination, entry) {
    try {
      if (entry.stompSub) {
        try {
          entry.stompSub.unsubscribe();
        } catch (e) {}
      }

      entry.stompSub = this.client.subscribe(destination, (message) => {
        try {
          const parsed = JSON.parse(message.body);
          entry.listeners.forEach((listener) => {
            try {
              listener(parsed);
            } catch (err) {
              console.error('[STOMP] Error in subscription callback:', err);
            }
          });
        } catch (err) {
          console.error('[STOMP] Error parsing message body:', err);
        }
      });
    } catch (e) {
      console.error('[STOMP] Error creating subscription for', destination, e);
    }
  }

  send(destination, body) {
    if (!this.client || !this.client.connected) {
      console.warn('[STOMP] Cannot send message: not connected');
      return false;
    }

    try {
      this.client.publish({
        destination,
        body: JSON.stringify(body),
      });
      return true;
    } catch (e) {
      console.error('[STOMP] Send error:', e);
      return false;
    }
  }
}

export const websocketService = new WebSocketService();
export default websocketService;
