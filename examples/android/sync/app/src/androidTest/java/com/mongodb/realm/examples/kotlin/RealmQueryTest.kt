package com.mongodb.realm.examples.kotlin

import android.util.Log
import com.mongodb.realm.examples.Expectation
import com.mongodb.realm.examples.RealmTest
import com.mongodb.realm.examples.YOUR_APP_ID
import com.mongodb.realm.examples.getRandomPartition
import com.mongodb.realm.examples.model.Dog
import com.mongodb.realm.examples.model.Person
import io.realm.Realm
import io.realm.kotlin.where
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.Credentials
import io.realm.mongodb.sync.SyncConfiguration
import org.bson.types.ObjectId
import org.junit.Assert
import org.junit.Test

class RealmQueryTest : RealmTest() {
    @Test
    fun testFindObjectByPrimaryKey() {
        val PARTITION = getRandomPartition();
        val expectation : Expectation = Expectation()
        activity?.runOnUiThread {
            val appID = YOUR_APP_ID // replace this with your App ID
            val app: App = App(
                AppConfiguration.Builder(appID)
                    .build()
            )

            val anonymousCredentials: Credentials = Credentials.anonymous()

            app.loginAsync(anonymousCredentials) {
                if (it.isSuccess) {
                    Log.v("EXAMPLE", "Successfully authenticated anonymously.")
                    // :code-block-start: find-object-by-primary-key
                    val config = SyncConfiguration.Builder(app.currentUser(), PARTITION)
                        .allowWritesOnUiThread(true)
                        .allowQueriesOnUiThread(true)
                        .build()

                    Realm.getInstanceAsync(config, object : Realm.Callback() {
                        override fun onSuccess(realm: Realm) {
                            Log.v(
                                "EXAMPLE",
                                "Successfully opened a realm with reads and writes allowed on the UI thread."
                            )

                            // :hide-start:
                            var PRIMARY_KEY_VALUE : ObjectId? = null
                            realm.executeTransaction { transactionRealm ->
                                val newTask = Task("test task")
                                transactionRealm.insert(newTask)
                                PRIMARY_KEY_VALUE = newTask._id
                            }
                            // :hide-end:

                            realm.executeTransaction { transactionRealm ->
                                val task = transactionRealm.where<Task>().equalTo("_id", PRIMARY_KEY_VALUE).findFirst()
                                Log.v("EXAMPLE", "Found object by primary key: $task")
                                // :hide-start:
                                Assert.assertEquals(task?._id, PRIMARY_KEY_VALUE)
                                // :hide-end:
                            }
                            // :hide-start:
                            expectation.fulfill()
                            // :hide-end:
                            realm.close()
                        }
                    })
                    // :code-block-end:
                } else {
                    Log.e("EXAMPLE", it.error.toString())
                }
            }
        }
        expectation.await()
    }

    @Test
    fun testQueryARelationship() {
        val expectation : Expectation = Expectation()
        val PARTITION = getRandomPartition();
        activity?.runOnUiThread {
            val appID = YOUR_APP_ID // replace this with your App ID
            val app: App = App(
                AppConfiguration.Builder(appID)
                    .build()
            )

            val anonymousCredentials: Credentials = Credentials.anonymous()

            app.loginAsync(anonymousCredentials) {
                if (it.isSuccess) {
                    Log.v("EXAMPLE", "Successfully authenticated anonymously.")
                    // :code-block-start: query-a-relationship
                    val config = SyncConfiguration.Builder(app.currentUser(), PARTITION)
                        .allowQueriesOnUiThread(true)
                        .allowWritesOnUiThread(true)
                        .build()

                    Realm.getInstanceAsync(config, object : Realm.Callback() {
                        override fun onSuccess(realm: Realm) {
                            Log.v(
                                "EXAMPLE",
                                "Successfully opened a realm with reads and writes allowed on the UI thread."
                            )

                            // :hide-start:
                            realm.executeTransaction { transactionRealm -> ;
                                val newDog = Dog("henry")
                                val newPerson = Person("dwayne")
                                newPerson.dog = newDog
                                transactionRealm.insert(newPerson)
                            }
                            // :hide-end:

                            realm.executeTransaction { transactionRealm ->
                                val owner = transactionRealm.where<Person>().equalTo("dog.name", "henry").findFirst()
                                val dog = owner?.dog
                                Log.v("EXAMPLE", "Queried for people with dogs named 'henry'. Found $owner, owner of $dog")
                                // :hide-start:
                                Assert.assertEquals(dog?.name, "henry")
                                Assert.assertEquals(owner?.name, "dwayne")
                                // :hide-end:
                            }
                            // :hide-start:
                            expectation.fulfill()
                            // :hide-end:
                            realm.close()
                        }
                    })
                    // :code-block-end:
                } else {
                    Log.e("EXAMPLE", it.error.toString())
                }
            }
        }
        expectation.await()
    }

    @Test
    fun testQueryAnInverseRelationship() {
        val expectation : Expectation = Expectation()
        val PARTITION = getRandomPartition();
        activity?.runOnUiThread {
            val appID = YOUR_APP_ID // replace this with your App ID
            val app: App = App(
                AppConfiguration.Builder(appID)
                    .build()
            )

            val anonymousCredentials: Credentials = Credentials.anonymous()

            app.loginAsync(anonymousCredentials) {
                if (it.isSuccess) {
                    Log.v("EXAMPLE", "Successfully authenticated anonymously.")
                    // :code-block-start: query-an-inverse-relationship
                    val config = SyncConfiguration.Builder(app.currentUser(), PARTITION)
                        .allowQueriesOnUiThread(true)
                        .allowWritesOnUiThread(true)
                        .build()

                    Realm.getInstanceAsync(config, object : Realm.Callback() {
                        override fun onSuccess(realm: Realm) {
                            Log.v(
                                "EXAMPLE",
                                "Successfully opened a realm with reads and writes allowed on the UI thread."
                            )

                            // :hide-start:
                            realm.executeTransaction { transactionRealm ->
                                val newDog = Dog("henry")
                                val newPerson = Person("dwayne")
                                newPerson.dog = newDog
                                transactionRealm.insert(newPerson)
                            }
                            // :hide-end:

                            realm.executeTransaction { transactionRealm ->
                                val dog = transactionRealm.where<Dog>() // :emphasize:
                                    .equalTo("owner.name", "dwayne").findFirst() // :emphasize:
                                val owner = dog?.owner?.first() // :emphasize:
                                Log.v("EXAMPLE", "Queried for dogs with owners named 'dwayne'. Found $dog, owned by $owner")
                            }
                            // :hide-start:
                            expectation.fulfill()
                            // :hide-end:
                            realm.close()
                        }
                    })
                    // :code-block-end:
                } else {
                    Log.e("EXAMPLE", it.error.toString())
                }
            }
        }
        expectation.await()
    }

}