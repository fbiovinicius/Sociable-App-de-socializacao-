const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const {
  collection,
  collectionGroup,
  doc,
  documentId,
  getDoc,
  getDocs,
  limit,
  orderBy,
  query,
  runTransaction,
  serverTimestamp,
  setDoc,
  startAfter,
  where,
  writeBatch,
} = require("firebase/firestore");

const rulesPath = path.resolve(__dirname, "../../firestore.rules");
let assertionCount = 0;

async function succeeds(operation) {
  await assertSucceeds(operation);
  assertionCount += 1;
}

async function fails(operation) {
  await assertFails(operation);
  assertionCount += 1;
}

async function run() {
  const testEnv = await initializeTestEnvironment({
    projectId: "demo-sociable",
    firestore: { rules: fs.readFileSync(rulesPath, "utf8") },
  });

  try {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      const db = context.firestore();
      await setDoc(doc(db, "users", "user-1"), {
        name: "Ana Souza",
        email: "ana@example.com",
        photoUrl: null,
        createdAt: serverTimestamp(),
      });
      await setDoc(doc(db, "users", "user-2"), {
        name: "Bruno Lima",
        email: "bruno@example.com",
        photoUrl: null,
        createdAt: serverTimestamp(),
      });
      await setDoc(doc(db, "users", "user-3"), {
        name: "Carla Reis",
        email: "carla@example.com",
        photoUrl: null,
        createdAt: serverTimestamp(),
      });
      await setDoc(doc(db, "users", "user-http"), {
        name: "Perfil legado",
        email: "legacy@example.com",
        photoUrl: "http://example.com/photo.jpg",
        createdAt: serverTimestamp(),
      });
      await setDoc(doc(db, "events", "public-event"), {
        organizerId: "organizer",
        status: "ACTIVE",
        goingCount: 0,
        capacity: null,
        isSecret: false,
        title: "Rolê público",
        createdAt: serverTimestamp(),
      });
      await setDoc(doc(db, "events", "rsvp-only-event"), {
        organizerId: "organizer",
        status: "ACTIVE",
        goingCount: 0,
        capacity: null,
        isSecret: false,
        title: "RSVP sem participante",
        createdAt: serverTimestamp(),
      });
      await setDoc(doc(db, "events", "https-event"), {
        organizerId: "organizer",
        status: "ACTIVE",
        goingCount: 0,
        capacity: null,
        isSecret: false,
        title: "Foto com HTTPS em maiúsculas",
        createdAt: serverTimestamp(),
      });
      await setDoc(doc(db, "events", "secret-event"), {
        organizerId: "organizer",
        status: "ACTIVE",
        goingCount: 0,
        capacity: null,
        isSecret: true,
        title: "Rolê secreto",
        createdAt: serverTimestamp(),
      });
      await setDoc(doc(db, "events", "secret-event-existing"), {
        organizerId: "organizer",
        status: "ACTIVE",
        goingCount: 1,
        capacity: null,
        isSecret: true,
        title: "Rolê secreto com participante",
        createdAt: serverTimestamp(),
      });
      await setDoc(doc(db, "events", "secret-event-existing", "rsvps", "user-1"), {
        userId: "user-1",
        checkedIn: false,
        createdAt: serverTimestamp(),
      });
      await setDoc(doc(db, "events", "secret-event-existing", "attendees", "user-1"), {
        userId: "user-1",
        name: "Ana Souza",
        photoUrl: null,
        createdAt: serverTimestamp(),
      });
      await setDoc(doc(db, "events", "secret-event-other"), {
        organizerId: "organizer",
        status: "ACTIVE",
        goingCount: 1,
        capacity: null,
        isSecret: true,
        title: "Outro rolê secreto",
        createdAt: serverTimestamp(),
      });
      await setDoc(doc(db, "events", "secret-event-other", "rsvps", "user-3"), {
        userId: "user-3",
        checkedIn: false,
        createdAt: serverTimestamp(),
      });
      await setDoc(doc(db, "events", "secret-event-other", "attendees", "user-3"), {
        userId: "user-3",
        name: "Carla Reis",
        photoUrl: null,
        createdAt: serverTimestamp(),
      });
      for (let index = 1; index <= 31; index += 1) {
        const eventId = `pagination-event-${index}`;
        await setDoc(doc(db, "events", eventId), {
          organizerId: "organizer",
          status: "ACTIVE",
          goingCount: 1,
          capacity: null,
          isSecret: false,
          title: `Rolê ${index}`,
          createdAt: serverTimestamp(),
        });
        await setDoc(doc(db, "events", eventId, "rsvps", "user-1"), {
          userId: "user-1",
          checkedIn: false,
          createdAt: serverTimestamp(),
        });
        await setDoc(doc(db, "events", eventId, "attendees", "user-1"), {
          userId: "user-1",
          name: "Ana Souza",
          photoUrl: null,
          createdAt: serverTimestamp(),
        });
      }
    });

    const user1 = testEnv.authenticatedContext("user-1").firestore();
    const user2 = testEnv.authenticatedContext("user-2").firestore();
    const user3 = testEnv.authenticatedContext("user-3").firestore();
    const httpUser = testEnv.authenticatedContext("user-http").firestore();
    const upperUser = testEnv.authenticatedContext("user-upper").firestore();

    // Primeiro RSVP em evento secreto: a transação precisa consultar o
    // próprio participante antes de criá-lo. Essa leitura não pode depender
    // de um RSVP que ainda não existe.
    await succeeds(
      getDoc(doc(user2, "events", "secret-event", "attendees", "user-2")),
    );
    await succeeds(
      runTransaction(user2, async (transaction) => {
        const eventReference = doc(user2, "events", "secret-event");
        const rsvpReference = doc(
          user2,
          "events",
          "secret-event",
          "rsvps",
          "user-2",
        );
        const attendeeReference = doc(
          user2,
          "events",
          "secret-event",
          "attendees",
          "user-2",
        );
        const userReference = doc(user2, "users", "user-2");

        await transaction.get(eventReference);
        await transaction.get(rsvpReference);
        await transaction.get(attendeeReference);
        await transaction.get(userReference);

        transaction.set(rsvpReference, {
          userId: "user-2",
          checkedIn: false,
          createdAt: serverTimestamp(),
        });
        transaction.update(eventReference, { goingCount: 1 });
        transaction.set(attendeeReference, {
          userId: "user-2",
          name: "Bruno Lima",
          photoUrl: null,
          createdAt: serverTimestamp(),
        });
      }),
    );
    await succeeds(
      getDoc(doc(user2, "events", "secret-event", "attendees", "user-2")),
    );
    await succeeds(
      getDocs(query(collection(user2, "events", "secret-event", "attendees"), limit(30))),
    );
    await fails(
      getDocs(query(collection(user3, "events", "secret-event", "attendees"), limit(30))),
    );

    // A sincronização de perfil filtra pelo userId armazenado no documento.
    // Ela pode atravessar eventos secretos, mas só retorna a própria pessoa.
    const ownAttendees = await getDocs(
      query(
        collectionGroup(user2, "attendees"),
        where("userId", "==", "user-2"),
        orderBy(documentId()),
        limit(30),
      ),
    );
    assertionCount += 1;
    assert.deepEqual(
      ownAttendees.docs.map((attendee) => attendee.ref.path),
      ["events/secret-event/attendees/user-2"],
    );
    await fails(
      getDocs(
        query(
          collectionGroup(user2, "attendees"),
          orderBy(documentId()),
          limit(30),
        ),
      ),
    );

    const firstAttendeePage = await getDocs(
      query(
        collectionGroup(user1, "attendees"),
        where("userId", "==", "user-1"),
        orderBy(documentId()),
        limit(30),
      ),
    );
    const secondAttendeePage = await getDocs(
      query(
        collectionGroup(user1, "attendees"),
        where("userId", "==", "user-1"),
        orderBy(documentId()),
        startAfter(firstAttendeePage.docs.at(-1)),
        limit(30),
      ),
    );
    assertionCount += 1;
    assert.equal(firstAttendeePage.size, 30);
    assert.ok(secondAttendeePage.size > 0);
    assert.notEqual(
      firstAttendeePage.docs.at(-1).ref.path,
      secondAttendeePage.docs[0].ref.path,
    );

    const confirmBatch = writeBatch(user1);
    confirmBatch.set(doc(user1, "events", "public-event", "rsvps", "user-1"), {
      userId: "user-1",
      checkedIn: false,
      createdAt: serverTimestamp(),
    });
    confirmBatch.update(doc(user1, "events", "public-event"), { goingCount: 1 });
    confirmBatch.set(doc(user1, "events", "public-event", "attendees", "user-1"), {
      userId: "user-1",
      name: "Ana Souza",
      photoUrl: null,
      createdAt: serverTimestamp(),
    });
    await succeeds(confirmBatch.commit());

    await succeeds(getDoc(doc(user1, "events", "public-event", "attendees", "user-1")));
    await succeeds(
      getDocs(query(collection(user1, "events", "public-event", "attendees"), limit(30))),
    );
    await fails(
      getDocs(query(collection(user1, "events", "public-event", "attendees"), limit(31))),
    );
    await fails(
      getDocs(collection(user1, "events", "public-event", "attendees")),
    );

    // O RSVP e o contador continuam atômicos: um cliente legado que não cria
    // o participante público precisa ser rejeitado.
    const rsvpOnlyBatch = writeBatch(user1);
    rsvpOnlyBatch.set(doc(user1, "events", "rsvp-only-event", "rsvps", "user-1"), {
      userId: "user-1",
      checkedIn: false,
      createdAt: serverTimestamp(),
    });
    rsvpOnlyBatch.update(doc(user1, "events", "rsvp-only-event"), { goingCount: 1 });
    await fails(rsvpOnlyBatch.commit());

    const attendeeOnlyBatch = writeBatch(user1);
    attendeeOnlyBatch.set(doc(user1, "events", "rsvp-only-event", "attendees", "user-1"), {
      userId: "user-1",
      name: "Ana Souza",
      photoUrl: null,
      createdAt: serverTimestamp(),
    });
    await fails(attendeeOnlyBatch.commit());

    const spoofBatch = writeBatch(user1);
    spoofBatch.update(doc(user1, "events", "public-event", "attendees", "user-1"), {
      name: "Perfil falso",
    });
    await fails(spoofBatch.commit());

    await fails(
      getDoc(doc(user3, "events", "secret-event-existing", "attendees", "user-1")),
    );
    await succeeds(
      getDoc(doc(user1, "events", "secret-event-existing", "attendees", "user-1")),
    );

    await succeeds(
      setDoc(doc(user1, "events", "public-event", "reports", "user-1"), {
        eventId: "public-event",
        reporterId: "user-1",
        reason: "SPAM_OR_SCAM",
        comment: "Golpe",
        createdAt: serverTimestamp(),
      }),
    );
    await succeeds(getDoc(doc(user1, "events", "public-event", "reports", "user-1")));
    await fails(
      getDocs(collection(user1, "events", "public-event", "reports")),
    );
    await fails(
      setDoc(doc(user1, "events", "missing-event", "reports", "user-1"), {
        eventId: "missing-event",
        reporterId: "user-1",
        reason: "OTHER",
        comment: "",
        createdAt: serverTimestamp(),
      }),
    );

    await succeeds(
      setDoc(doc(upperUser, "users", "user-upper"), {
        name: "HTTPS Maiúsculo",
        photoUrl: "HTTPS://example.com/photo.jpg",
      }),
    );
    const upperPhotoBatch = writeBatch(upperUser);
    upperPhotoBatch.set(doc(upperUser, "events", "https-event", "rsvps", "user-upper"), {
      userId: "user-upper",
      checkedIn: false,
      createdAt: serverTimestamp(),
    });
    upperPhotoBatch.update(doc(upperUser, "events", "https-event"), { goingCount: 1 });
    upperPhotoBatch.set(doc(upperUser, "events", "https-event", "attendees", "user-upper"), {
      userId: "user-upper",
      name: "HTTPS Maiúsculo",
      photoUrl: "HTTPS://example.com/photo.jpg",
      createdAt: serverTimestamp(),
    });
    await succeeds(upperPhotoBatch.commit());

    await fails(
      setDoc(doc(user1, "users", "user-new"), {
        name: "Pessoa Sem HTTPS",
        photoUrl: "http://example.com/photo.jpg",
      }),
    );

    const httpPhotoBatch = writeBatch(httpUser);
    httpPhotoBatch.set(doc(httpUser, "events", "rsvp-only-event", "rsvps", "user-http"), {
      userId: "user-http",
      checkedIn: false,
      createdAt: serverTimestamp(),
    });
    httpPhotoBatch.update(doc(httpUser, "events", "rsvp-only-event"), { goingCount: 1 });
    httpPhotoBatch.set(doc(httpUser, "events", "rsvp-only-event", "attendees", "user-http"), {
      userId: "user-http",
      name: "Perfil legado",
      photoUrl: "http://example.com/photo.jpg",
      createdAt: serverTimestamp(),
    });
    await fails(httpPhotoBatch.commit());

    const cancelBatch = writeBatch(user1);
    cancelBatch.delete(doc(user1, "events", "public-event", "rsvps", "user-1"));
    cancelBatch.update(doc(user1, "events", "public-event"), { goingCount: 0 });
    cancelBatch.delete(doc(user1, "events", "public-event", "attendees", "user-1"));
    await succeeds(cancelBatch.commit());

    console.log(`rules-ok: ${assertionCount} assertions passed`);
  } finally {
    await testEnv.cleanup();
  }
}

run().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
