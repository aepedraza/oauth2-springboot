package com.appsdeveloperblog.ws.clients.photoappwebclient.controllers;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.appsdeveloperblog.ws.clients.photoappwebclient.response.AlbumRest;

// Not a REST controller, just a regular MVC controller
@Controller
public class AlbumsController {
	
	@Autowired
	OAuth2AuthorizedClientService oauth2ClientService;
	
	@Autowired
	RestTemplate restTemplate;

	@Autowired
	WebClient webClient;
	

	// to trigger this method when GET /albums is requested from browser
	@GetMapping("/albums")
	public String getAlbums(
			Model model, // Model for thymeleaf template (view)
			@AuthenticationPrincipal OidcUser principal // Currently authenticated user
	) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;

		OAuth2AuthorizedClient oAuth2Client = oauth2ClientService.loadAuthorizedClient(oauth2Token.getAuthorizedClientRegistrationId(), oauth2Token.getName());
		String jwtAccessToken = oAuth2Client.getAccessToken().getTokenValue();
		System.out.println("jwtAccessToken: " + jwtAccessToken);

		System.out.println("Principal: " + principal);
		OidcIdToken idToken = principal.getIdToken();
		String tokenValue = idToken.getTokenValue();
		System.out.println("idTokenValue: " + tokenValue);

		String albumsApiGatewayUrl = "http://localhost:8082/albums";
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(jwtAccessToken);
		HttpEntity<Object> entity = new HttpEntity<>(headers);

		ResponseEntity<List<AlbumRest>> responseEntity = restTemplate.exchange(albumsApiGatewayUrl, HttpMethod.GET, entity,
				new ParameterizedTypeReference<List<AlbumRest>>() {});

		List<AlbumRest> albums = responseEntity.getBody();

//		List<AlbumRest> albums = webClient.get()
//				.uri(albumsApiGatewayUrl)
//				.retrieve()
//				.bodyToMono(new ParameterizedTypeReference<List<AlbumRest>>(){})
//				.block();
	
        model.addAttribute("albums", albums);
		
		
		return "albums";
	}
	
}
