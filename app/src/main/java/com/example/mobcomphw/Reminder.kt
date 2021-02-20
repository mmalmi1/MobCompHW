package com.example.mobcomphw

class Reminder {
    var message : String?
    var location_x : String?
    var location_y : String?
    var reminder_time : String?
    var creation_time : String?
    var creator_id : String?
    var reminder_seen : Boolean?
    var reminder_type : String?

    constructor(
        message: String?,
        location_x: String?,
        location_y: String?,
        reminder_time: String?,
        creation_time: String?,
        creator_id: String?,
        reminder_seen: Boolean?,
        reminder_type: String?
    ) {
        this.message = message
        this.location_x = location_x
        this.location_y = location_y
        this.reminder_time = reminder_time
        this.creation_time = creation_time
        this.creator_id = creator_id
        this.reminder_seen = reminder_seen
        this.reminder_type = reminder_type
    }
}