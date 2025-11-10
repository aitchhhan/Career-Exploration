package career.exploration.dto.Response;


import career.exploration.enums.RoleType;

public record LoginInfoRes(String name, String email, RoleType roleType) {
}
