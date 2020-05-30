function check_firm_name(){
    if(($("#corp_name").val()=="")){
        $("#corp_name").css("border-color", "#F05A5A");
        $("#corp_name_tip").text("请输入用户名");
        $("#corp_name_tip").css("color", "#F05A5A");
        return false;
    }else{
        firm_name_recovery();
        return true;
    }
}
function firm_name_recovery(){
    $("#corp_name").css("border-color", "#C7C7C7");
    $("#corp_name_tip").text("请填写用户名");
    $("#corp_name_tip").css("color", "#7F7F7F");
}


function check_legal_person_name(){
    if(($("#legal_person_name").val()=="")){
        $("#legal_person_name").css("border-color", "#F05A5A");
        $("#legal_person_name_tip").text("请输入密码");
        $("#legal_person_name_tip").css("color", "#F05A5A");
        return false;
    }else{
        firm_name_recovery();
        return true;
    }
}
function legal_person_name_recovery(){
    $("#legal_person_name").css("border-color", "#C7C7C7");
    $("#legal_person_name_tip").text("请输入密码");
    $("#legal_person_name_tip").css("color", "#7F7F7F");
}

function submit(){
    var flag = check_firm_name();
    flag = check_legal_person_name() && flag;
    if(flag){
        console.log("全部填写")
        $.ajax({
            type : "POST",
            url : "/system/register",
            data : {
                name : $("#corp_name").val(),
                password : $("#legal_person_name").val()
            },
            success : function(json) {
                if (json.code == "200"){
                    window.location.href = "login.html";
                } else {
                    // alert("您所请求的页面有异常");
                    console.log(json.msg)
                    console.log("状态码错误")
                    $("#submit_tip").text(json.msg)
                }
            },
            error : function(jqXHR) {
                // alert("您所请求的页面有异常");
                console.log("错误")
                $("#submit_tip").text("您所请求的页面有异常")
            }
        })
    }else{
        console.log("未全部填写")
    }
}