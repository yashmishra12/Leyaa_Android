const functions = require("firebase-functions");
const admin = require("firebase-admin");

// Load the service account JSON from functions/keys/service-account.json
const serviceAccount = require("./keys/service-account.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

// HTTP endpoint: /sendNotification
exports.sendNotification = functions.https.onRequest(function (req, res) {
  // Only allow POST
  if (req.method !== "POST") {
    res.status(405).send("Method Not Allowed");
    return;
  }

  const token = req.body.token;
  const title = req.body.title;
  const body = req.body.body;

  if (!token || !title || !body) {
    res.status(400).json({ success: false, error: "Missing token/title/body" });
    return;
  }

  const message = {
    token: token,
    notification: {
      title: title,
      body: body,
    },
  };

  admin
    .messaging()
    .send(message)
    .then(function (response) {
      console.log("Successfully sent message:", response);
      res.status(200).json({ success: true, response: response });
    })
    .catch(function (error) {
      console.error("Error sending message:", error);
      res.status(500).json({ success: false, error: error.toString() });
    });
});
