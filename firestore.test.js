const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
const DOCTOR_UID = "doctor_tabib_123";
const FOLLOWER_ALICE = "alice_follower_456";
const ATTACKER_BOB = "bob_attacker_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// --- TESTS ---

test("Unauthenticated user: cannot read or write restaurants", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("restaurants").get());
  await assertFails(
    unauthDb.collection("restaurants").doc("rest_1").set({
      name: "شواية القصبة",
      category: "مشاوي",
      city: "الجزائر",
      doctorRating: 9.0,
      doctorVerdict: "ممتاز",
      authorId: "any",
      createdAt: new Date(),
    })
  );
});

test("Authenticated Doctor: can create restaurant with valid payload and own authorId", async () => {
  const docDb = testEnv.authenticatedContext(DOCTOR_UID).firestore();
  await assertSucceeds(
    docDb.collection("restaurants").doc("rest_1").set({
      name: "شواية القصبة الأصيلة",
      category: "مشاوي وشواية",
      city: "الجزائر العاصمة",
      doctorRating: 9.5,
      doctorVerdict: "تشخيص ممتاز ولحم طري ومتبل صح",
      authorId: DOCTOR_UID,
      createdAt: new Date(),
    })
  );
});

test("Attacker: cannot create restaurant with spoofed authorId", async () => {
  const bobDb = testEnv.authenticatedContext(ATTACKER_BOB).firestore();
  await assertFails(
    bobDb.collection("restaurants").doc("rest_spoofed").set({
      name: "مطعم وهمي",
      category: "مشاوي",
      city: "الجزائر",
      doctorRating: 9.0,
      doctorVerdict: "تزوير",
      authorId: DOCTOR_UID, // Spoofed doctor UID
      createdAt: new Date(),
    })
  );
});

test("Follower: can read restaurants and add their own review", async () => {
  // Pre-seed restaurant by Doctor
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("restaurants").doc("rest_1").set({
      name: "برجر كيتشن",
      category: "برجر",
      city: "وهران",
      doctorRating: 8.5,
      doctorVerdict: "جيد جدا",
      authorId: DOCTOR_UID,
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(FOLLOWER_ALICE).firestore();
  
  // Alice can read
  await assertSucceeds(aliceDb.collection("restaurants").doc("rest_1").get());

  // Alice adds follower review
  await assertSucceeds(
    aliceDb.collection("restaurants").doc("rest_1").collection("reviews").doc("rev_1").set({
      restaurantId: "rest_1",
      followerId: FOLLOWER_ALICE,
      followerName: "أليس العاصمية",
      rating: 9.0,
      comment: "أتفق مع الطبيب تماماً، البرجر كان رائعاً والخدمة سريعة",
      agreement: "متفق تماماً مع الطبيب",
      dishOrdered: "دبل تشيز برجر",
      createdAt: new Date(),
    })
  );
});

test("Another follower: can increment helpfulCount on review", async () => {
  // Pre-seed restaurant and Alice's review
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("restaurants").doc("rest_1").set({
      name: "سمك الميناء",
      category: "مأكولات بحرية",
      city: "عنابة",
      doctorRating: 9.2,
      doctorVerdict: "طازج ولذيذ",
      authorId: DOCTOR_UID,
      createdAt: new Date(),
    });

    await context.firestore().collection("restaurants").doc("rest_1").collection("reviews").doc("rev_1").set({
      restaurantId: "rest_1",
      followerId: FOLLOWER_ALICE,
      followerName: "أليس",
      rating: 9.0,
      comment: "تجربة ممتازة",
      helpfulCount: 5,
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(ATTACKER_BOB).firestore();
  
  // Bob can increment helpfulCount
  await assertSucceeds(
    bobDb.collection("restaurants").doc("rest_1").collection("reviews").doc("rev_1").update({
      helpfulCount: 6,
    })
  );

  // But Bob CANNOT modify the review's text comment!
  await assertFails(
    bobDb.collection("restaurants").doc("rest_1").collection("reviews").doc("rev_1").update({
      comment: "تعليق محرف من طرف مستخدم آخر",
    })
  );
});
