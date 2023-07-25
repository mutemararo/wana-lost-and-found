package com.wanalnf.wana_lost_and_found.model

enum class ReportType(val identity: String){
    LOST("lost"),
    FOUND("found"),
    PLACEHOLDER("fillIn");
}

enum class ReportStatus(val status : String){
    REPORTED("reported"),
    CLAIMED("claimed"),
    FOUND("found"),
    COLLECTED("collected");
}

class Report {
    var reportID : String = ""
    var reportedBy : String = ""
    var itemName: String = ""
    var itemImage: String = ""
    var description : String = ""
    var tagLabel : String = ""
    var country : String = ""
    var city : String = ""
    var reportType : String = ReportType.PLACEHOLDER.identity
    var reportStatus : String = ReportStatus.REPORTED.status
    var submitted : Boolean = false
    var nameOfAuthority : String = ""
    var locationOfAuthority: String = ""
    var contactOfAuthority : String = ""
    var dateReported : String = ""
    var responseTo : String = ""
    var dateResponded : String = ""
}