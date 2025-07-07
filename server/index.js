// index.js
import express from 'express';
import { sendPush } from './fcm.js';
import admin from 'firebase-admin';
import fs from 'fs';

// 🔐 service-account.json 직접 읽어서 파싱
const serviceAccount = JSON.parse(
    fs.readFileSync('./service-account.json', 'utf8')
);

const app = express();
app.use(express.json());

// Firebase admin 초기화
if (!admin.apps.length) {
    admin.initializeApp({
        credential: admin.credential.cert(serviceAccount)
    });
}

const db = admin.firestore();


// 서버 상태 확인용 GET
app.get('/', (_, res) => {
    res.send('🥼 Glucose push server is running.');
});

// FCM 푸시 전송용 POST
app.post('/send', async (req, res) => {
    const { glucose, token } = req.body;

    if (glucose === undefined || glucose === null) {
        return res.status(400).json({ error: 'Missing glucose value in request body' });
    }

    if (!token || typeof token !== 'string') {
        return res.status(400).json({ error: 'Missing or invalid FCM token' });
    }

    try {
        // 1. FCM 푸시 전송
        await sendPush(glucose, token);

        // 2. Firestore에 혈당 수치 저장
        await db.collection('glucoseLogs').add({
            value: glucose,
            timestamp: admin.firestore.FieldValue.serverTimestamp()
        });

        console.log(`✅ 푸시 전송 요청 수신: 혈당 ${glucose}, 토큰 일부: ${token.slice(0, 10)}...`);
        res.status(200).json({ message: 'Push notification sent successfully' });
    } catch (err) {
        console.error('❌ 푸시 전송 실패:', err);
        res.status(500).json({ error: 'Failed to send push notification' });
    }
});

// 서버 시작
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`✅ Server listening on http://localhost:${PORT}`);
});
