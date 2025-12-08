package com.backend.Backend.service;
import com.backend.Backend.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;


import java.util.Date;



@Service
public class JWTService {

    private final String SECRET = "4e43fbefa92e77a3142601c9e4e3c6314636e3fa9d3cd9a145f6052b694a7b6f";
    private final long EXP = 1000 * 60 * 60 * 24;

    public String generate(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole())
                .setExpiration(new Date(System.currentTimeMillis() + EXP))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    public String validate(String token) {
        System.out.println(token);
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();



    }



}
