package gg.users.userapps.domain.model.commands;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserResponse {
    private String message;
    private Integer code;
}
