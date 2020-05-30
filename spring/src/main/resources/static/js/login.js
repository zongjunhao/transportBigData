function check_user_name(){   
    if($("#user_name").val()==""){
        if(!$("#user_name").is(":focus")){
            $("#user_name").css("border-color", "#F05A5A");
            $("#user_tip").css("color", "#F05A5A");
        }
        return false;
    }else{
        $("#user_name").css("border-color", "#C7C7C7");
        $("#user_tip").css("color", "#FFFFFF");
        return true;
    }
}
function user_name_recovery(){
    $("#user_name").css("border-color", "#2984EF");
    $("#user_tip").css("color", "#FFFFFF");
}


function check_user_pwd(){
    if(($("#user_password").val()=="")){
        $("#user_password").css("border-color", "#F05A5A");
        $("#user_pwd_tip").css("color", "#F05A5A");
        return false;
    }else{
        $("#user_password").css("border-color", "#C7C7C7");
        $("#user_pwd_tip").css("color", "#FFFFFF");
        return true;
    }
}
function user_pwd_recovery(){
    $("#user_password").css("border-color", "#2984EF");
    $("#user_pwd_tip").css("color", "#FFFFFF");
}

function user_login(){
    var flag = check_user_name();
    flag = check_user_pwd() && flag;
    flag = check_user_code() && flag;
    if(flag){
        $.ajax({
            type : "POST",
            url : "/system/login",
            data : {
                name : $("#user_name").val(),
                password : $("#user_password").val()
            },
            success : function(json) {
                console.log(json)
                if (json.code == "200") {
                    //登录成功
                    //alert(json.resultDesc);
                    $.session.set("u_id",json.data.id);
                    $.session.set("u_name",json.data.name);
                    window.location.href = "main.html";
                } else {
                    //登录失败
                    //alert(json.resultDesc);
                    $("#user_code_tip").text(json.msg);
                    $("#user_code_tip").css("color", "#F05A5A");
                }
            },
            error : function(jqXHR) {
                $("#user_code_tip").text("您所请求的页面有异常");
                $("#user_code_tip").css("color", "#F05A5A");
            }
        });
    }
}



