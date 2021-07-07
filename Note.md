MainActivity note

Snapshot Listener
   //snapshot listener already comes with callback function, lit means tinggal panggil "callback" func, karena..
        //job  dari function utama sudah dilakukan terlebih dahulu.
        //penggunaan coroutine tidak terlalu diperlukan dan tidak akan menganggu thread


/* Dispatcher IO
    untuk menjalankan disk atau I/O jaringan di luar thread utama.
    Contohnya termasuk menggunakan Komponen room, membaca dari atau menulis ke file, dan..
    menjalankan operasi jaringan apa pun. */


/* Await
    await for the completion of the task without blocking a thread */


/* KTX-Dependency (since we use ktx dep, we can change the following code :)
    documents.toObject(Biography::class.java) -> can work in java and kotlin
    documents.toObject<Biography>() -> only work in kotlin  */


MainActivity2

Transactions
/* set of read and write operations on one or more documents.
   if there's another client changing the same value of the data, the transaction will executed again to check (read) that, to ensure that the transaction runs on up-to-date and consistent data
   Transactions are useful when you want to update a field's value based on its current value, or the value of some other field.*/
	 

/* for realtime, it can simply add ".whereEqualTo/another query", same as sortDatabaseQueries function
   then add ".addSnapshotListener" */

SET OPTIONS. MERGE
/* Change the behaviour of set() calls to only replace the values specified in its data arg, not replace the entirely. */

// update
// db.document(documents.id).update("firstname", newFirstname) -> if only update one field data