package career.exploration.dto;


import career.exploration.enums.RoleType;

public record LoginInfoRes(String name, String email, RoleType roleType) {
}
