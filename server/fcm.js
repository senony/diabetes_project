// fcm.js
import admin from 'firebase-admin';
import fs from 'fs/promises';
import path from 'path';

// 서비스 계정 경로
const serviceAccountPath = path.join(process.cwd(), 'service-account.json');

// 서비스 계정 키 로드
const serviceAccount = JSON.parse(
    await fs.readFile(serviceAccountPath, 'utf-8')
);

// Firebase Admin SDK 초기화 (중복 방지)
if (!admin.apps.length) {
    admin.initializeApp({
        credential: admin.credential.cert(serviceAccount),
    });
}

/**
 * 푸시 메시지 전송 함수
 * @param {number} glucose - 혈당 수치
 * @param {string} targetToken - 수신 대상의 FCM 토큰
 */
export const sendPush = async (glucose, targetToken) => {
    const message = {
        token: targetToken,
        data: {
            title: '📢 할아버지 혈당 수치',
            body: `현재 혈당: ${glucose}`,
            glucose: glucose.toString()
        },
        android: {
            priority: 'high',
        },
    };

    try {
        const response = await admin.messaging().send(message);
        console.log('✅ FCM 전송 성공:', response);
    } catch (error) {
        console.error('❌ FCM 전송 실패:', error);
    }
};