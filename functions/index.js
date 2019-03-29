const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

//exports.addTimeStamp = functions.firestore
//   .document('chats/{chatId}/messages/{messageId}')
//   .onCreate((snap, context) => {
//     if (snap) {
//       return snap.ref.update({
//                   timestamp: admin.firestore.FieldValue.serverTimestamp()
//               });
//     }
//
//     return "snap was null or empty";
//   });
//
//exports.sendChatNotifications = functions.firestore
//   .document('chats/{chatId}/messages/{messageId}')
//   .onCreate((snap, context) => {
//     // Get an object with the current document value.
//     // If the document does not exist, it has been deleted.
//     const document = snap.exists ? snap.data() : null;
//
//     if (document) {
//       var message = {
//         notification: {
//           title: document.from + ' sent you a message',
//           body: document.text
//         },
//         topic: context.params.chatId
//       };
//
//       return admin.messaging().send(message)
//         .then((response) => {
//           // Response is a message ID string.
//           console.log('Successfully sent message:', response);
//           return response;
//         })
//         .catch((error) => {
//           console.log('Error sending message:', error);
//           return error;
//         });
//     }
//
//     return "document was null or emtpy";
//   });
//
//exports.sendPublicNotification = functions.firestore
//  .document('/chatroom/{roomName}/messages/{messageId}')
//  .onCreate((snap, context) => {
//    // Get an object with the current document value.
//    // If the document does not exist, it has been deleted.
//    const document = snap.exists ? snap.data() : null;
//
//    if (document) {
//      var message = {
//        notification: {
//          title: document.from + ' sent you a message',
//          body: document.text
//        },
//        topic: 'public_ntfcn'
//      };
//
//      return admin.messaging().send(message)
//        .then((response) => {
//          // Response is a message ID string.
//          console.log('Successfully sent message:', response);
//          return response;
//        })
//        .catch((error) => {
//          console.log('Error sending message:', error);
//          return error;
//        });
//    }
//
//    return "document was null or emtpy";
//  });


exports.sendPrivateNotification = functions.firestore
  .document('/chatroom/{roomName}/messages/{messageId}')
  .onCreate((snap, context) => {
    // Get an object with the current document value.
    // If the document does not exist, it has been deleted.
    const document = snap.exists ? snap.data() : null;

    if (document) {
      var message = {
        notification: {
          title: document.from + ' sent you a private message',
          body: document.text
//          click_action: "chatroom"

        },
        data:{
            click_action: "chatroom",
//            from: document.from,
            to: document.to,
            roomName: context.params.roomName
        }
    };

    console.log('Token to send notification:', document.token);
      return admin.messaging().sendToDevice(document.token, message)
        .then((response) => {
          // Response is a message ID string.
          console.log('Successfully sent message:', response.results[0].error);
          return response;
        })
        .catch((error) => {
          console.log('Error sending message:', error);
          return error;
        });
    }

    return "document was null or emtpy";
  });
