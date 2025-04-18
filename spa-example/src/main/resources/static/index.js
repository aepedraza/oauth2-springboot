function generateState(length) {
    var stateValue = "";
    var alphaNumericCharacters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    var alphaNumericCharactersLength = alphaNumericCharacters.length;
    for (var i = 0; i < length; i++) {
        stateValue += alphaNumericCharacters.charAt(Math.floor(Math.random() * alphaNumericCharactersLength));
    }

    $('#stateValue').html(stateValue);
}

function generateCodeVerifier() {
    var returnValue = "";
    var randomByteArray = new Uint8Array(32);
    window.crypto.getRandomValues(randomByteArray);

    returnValue = base64UrlEncode(randomByteArray);

    $('#codeVerifierValue').html(returnValue);
}

function base64UrlEncode(sourceValue) {
    var stringValue = String.fromCharCode.apply(null, sourceValue);
    var base64Encoded = btoa(stringValue);
    var base64urlEncoded = base64Encoded.replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
    return base64urlEncoded;
}

async function generateCodeChallenge() {
    var codeChallengeValue = "";

    var codeVerifier = $('#codeVerifierValue').html();

    // Challenge creation according to RFC
    var textEncoder = new TextEncoder('US-ASCII');
    var encodedValue = textEncoder.encode(codeVerifier);
    var digest = await window.crypto.subtle.digest("SHA-256", encodedValue);

    codeChallengeValue = base64UrlEncode(Array.from(new Uint8Array(digest)));

    $('#codeChallengeValue').html(codeChallengeValue);
}

function getAuthCode() {
    var state = $('#stateValue').html();
    var codeChallenge = $('#codeChallengeValue').html();

    // Build URL to redirect to Auth server
    var authorizationURL = "http://localhost:8080/realms/appsdeveloperblog/protocol/openid-connect/auth";
    authorizationURL += "?client_id=photo-app-PKCE-client";
    authorizationURL += "&response_type=code";
    authorizationURL += "&scope=openid"; // More scopes may be added
    authorizationURL += "&redirect_uri=http://localhost:8181/authCodeReader.html";
    authorizationURL += "&state=" + state;
    authorizationURL += "&code_challenge=" + codeChallenge;
    authorizationURL += "&code_challenge_method=S256";

    // Open Auth server in a new window
    window.open(authorizationURL, 'authorizationRequestWindow', 'width=800,height=600,left=200,top=200');
}

function postAuthorize(state, authCode) {
	var originalStateValue = $('#stateValue').html();

    if(state === originalStateValue) {
        requestTokens(authCode);
    } else {
        alert("Invalid state value received");
    }
}

function requestTokens(authCode) {
    var codeVerifier = $('#codeVerifierValue').html();
    var data = {
        "grant_type": "authorization_code",
        "client_id": "photo-app-PKCE-client",
        "code": authCode,
        "code_verifier": codeVerifier,
        "redirect_uri":"http://localhost:8181/authCodeReader.html"
    };

    // Sending POST request to Auth server to get a JWT
    $.ajax({
        beforeSend: function (request) {
            request.setRequestHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
        },
        type: "POST",
        url: "http://localhost:8080/realms/appsdeveloperblog/protocol/openid-connect/token",
        data: data,
        success: postRequestAccessToken,
        dataType: "json"
    });
}

function postRequestAccessToken(data, status, jqXHR) { // jqXHR: response object
    $('#accessToken').html(data["access_token"]);
}

function getInfoFromResourceServer(){

    var accessToken = $('#accessToken').html();

    $.ajax({ // Need to configure CORS in API GW and Resource Server
        beforeSend: function (request) {
            request.setRequestHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
            request.setRequestHeader("Authorization", "Bearer " + accessToken);
        },
        type: "GET",
        url: "http://localhost:8082/users/status/check", // send to API Gateway. Get info from ResourceServer
        success: postInfoFromAccessToken,
        dataType: "text"
    });
}

function postInfoFromAccessToken(data, status, jqXHR) {
    alert(data);
}
