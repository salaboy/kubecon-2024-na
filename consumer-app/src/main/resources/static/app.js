const jsConfetti = new JSConfetti()

function load() {

  var url = ""
  if (location.protocol === "http:") {
    url = 'ws://' + location.host + '/websocket'
  } else {
    url = 'wss://' + location.host + '/websocket'
  }

  const stompClient = new StompJs.Client({
    brokerURL: url
  });

  stompClient.activate();

  stompClient.onConnect = (frame) => {

    console.log('Connected: ' + frame);
    stompClient.subscribe('/topic/events', (event) => {
      count = JSON.parse(event.body)
      console.log(count);
      if(count.type === "sync"){
        jsConfetti.addConfetti({
                emojis: ['➡️'],
                emojiSize: 100,
                confettiNumber: 1,
         })
         document.getElementById("sync").innerHTML = "<h1> Sync Counter: <b>" + count.count + "</b><h1>";

      }

      if(count.type === "async") {
        jsConfetti.addConfetti({
                        emojis: ['✉️'],
                        emojiSize: 100,
                        confettiNumber: 1,
                 })
        document.getElementById("async").innerHTML = "<h1> Async Counter: <b>" + count.count + "</b><h1>";
      }

      if(count.type === "async-error") {
              jsConfetti.addConfetti({
                              emojis: ['☠️✉️'],
                              emojiSize: 100,
                              confettiNumber: 1,
                       })
            }
      if(count.type === "sync-error") {
                    jsConfetti.addConfetti({
                                    emojis: ['☠️➡️'],
                                    emojiSize: 100,
                                    confettiNumber: 1,
                             })
                  }
    });
  };

  stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
  };

  stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
  };
}

function breakSync(){
    fetch("/breakfixsync", {
              method: "POST",
              headers: {
                "Content-type": "application/json; charset=UTF-8"
              }
            }).then((response) => {
                console.log(response)
                jsConfetti.addConfetti({
                                        emojis: ['💔➡️'],
                                        emojiSize: 100,
                                        confettiNumber: 1,
                                 })
            })
}

function breakASync(){
    fetch("/breakfixasync", {
              method: "POST",
              headers: {
                "Content-type": "application/json; charset=UTF-8"
              }
            }).then((response) => {
                console.log(response)
                jsConfetti.addConfetti({
                                                        emojis: ['💔✉️️'],
                                                        emojiSize: 100,
                                                        confettiNumber: 1,
                                                 })
            })
}












