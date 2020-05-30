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
            url : "/KnowledgeLibrary/user/login",
            data : {
                u_mail : $("#user_name").val(),
                u_pwd : $("#user_password").val(),
                inputRandomCode : $("#user_inputRandomCode").val()
            },
            success : function(json) {
                console.log(json)
                if (json.resultCode == "4000") {
                    //登录成功
                    //alert(json.resultDesc);
                    $.session.set("u_age",json.data.u_age);
                    $.session.set("u_mail",json.data.u_email);
                    $.session.set("u_firm",json.data.u_firm);
                    $.session.set("u_gender",json.data.u_gender);
                    $.session.set("u_id",json.data.u_id);
                    $.session.set("u_name",json.data.u_name);
                    $.session.set("u_phone",json.data.u_phone);
                    $.session.set("u_pwd",json.data.u_pwd);
                    $.session.set("u_role",json.data.u_role);
                    let role=json.data.u_role+1;
                    console.log(role)
                    $.session.set("role", role);
                    console.log($.session.get("role"));
                    window.location.href = "index.html";
                } else {
                    //登录失败
                    //alert(json.resultDesc);
                    $("#user_code_tip").text(json.resultDesc);
                    $("#user_code_tip").css("color", "#F05A5A");
                    $("#user_pic").attr('src','user/pic?x='+Math.random()); 
                }
            },
            error : function(jqXHR) {
                $("#user_code_tip").text("您所请求的页面有异常");
                $("#user_code_tip").css("color", "#F05A5A");
            }
        });
    }
}



