package com.dev.nathan.kotlinanonymousmapsposts

import java.util.*


class AttestantPost {

    lateinit var user_id: String
    lateinit var image_url: String
    lateinit var desc: String
    lateinit var image_thumb: String
    lateinit var address: String
    lateinit var timestamp: Date

    constructor() {}

    constructor(user_id: String, image_url: String, desc: String, image_thumb: String, timestamp: Date, address: String) {
        this.user_id = user_id
        this.image_url = image_url
        this.desc = desc
        this.timestamp = timestamp
        this.image_thumb = image_thumb
        this.address = address
    }


}
