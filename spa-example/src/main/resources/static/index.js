function generateState(length) {
    var stateValue = "";
    var alphaNumericCharacters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    var alphaNumericCharactersLength = alphaNumericCharacters.length;
    for (var i = 0; i < length; i++) {
        stateValue += alphaNumericCharacters.charAt(Math.floor(Math.random() * alphaNumericCharactersLength));
    }

    document.getElementById("stateValue").innerHTML = stateValue;
}

function generateCodeVerifier() {
    var returnValue = "";
    var randomByteArray = new Uint8Array(32);
    window.crypto.getRandomValues(randomByteArray);

    returnValue = base64urlencode(randomByteArray);

    document.getElementById("codeVerifierValue").innerHTML = returnValue;
}

function base64urlencode(sourceValue) {
    var stringValue = String.fromCharCode.apply(null, sourceValue);
    var base64Encoded = btoa(stringValue);
    var base64urlEncoded = base64Encoded.replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
    return base64urlEncoded;
}

async function generateCodeChallenge() {
    var codeChallengeValue = "";

    var codeVerifier = document.getElementById("codeVerifierValue").innerHTML;

    var textEncoder = new TextEncoder('US-ASCII');
    var encodedValue = textEncoder.encode(codeVerifier);
    var digest = await window.crypto.subtle.digest("SHA-256", encodedValue);

    codeChallengeValue = base64urlencode(Array.from(new Uint8Array(digest)));

    document.getElementById("codeChallengeValue").innerHTML = codeChallengeValue;
}

function getAuthCode() {
    var state = document.getElementById("stateValue").innerHTML;
    var codeChallenge = document.getElementById("codeChallengeValue").innerHTML;

    // Build URL to redirect to Auth server
    var authorizationURL = "http://localhost:8080/auth/appsdeveloperblog/protocol/openid-connect/auth";
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
	var originalStateValue = document.getElementById("stateValue").innerHTML;

    if(state === originalStateValue) {
        requestTokens(authCode);
    } else {
        alert("Invalid state value received");
    }
}

function requestTokens(authCode) {
    var codeVerifier = document.getElementById("codeVerifierValue").innerHTML;
    var data = {
        "grant_type": "authorization_code",
        "client_id": "photo-app-PKCE-client",
        "code": authCode,
        "code_verifier": codeVerifier,
        "redirect_uri":"http://localhost:8181/authcodeReader.html"
    };

    // Sending POST request to Auth server to get a JWT
    $.ajax({
        beforeSend: function (request) {
            request.setRequestHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
        },
        type: "POST",
        url: "http://localhost:8080/auth/protocol/openid-connect/token",
        data: data,
        success: postRequestAccessToken,
        dataType: "json"
    });
}

function postRequestAccessToken(data, status, jqXHR) { // jqXHR: response object
    document.getElementById("accessToken").innerHTML = data["access_token"];
}
