const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

const TARGET_TOPIC = "caregiver";

exports.sendGlucoseAlert = functions.database
  .ref("/bloodSugar/{entryId}")
  .onCreate(async (snapshot, context) => {
    const value = snapshot.val();
    const glucose = value?.glucose;
    if (!glucose) return null;

    const payload = {
      notification: {
        title: "혈당 수치 입력됨",
        body: `할아버지의 혈당 수치: ${glucose}`,
      },
      topic: TARGET_TOPIC,
    };

    try {
      const response = await admin.messaging().send(payload);
      console.log("푸시 전송 성공:", response);
    } catch (error) {
      console.error("푸시 전송 실패:", error);
    }

    return null;
  });
