CREATE TABLE permissions (
    id UUID PRIMARY KEY,
    action VARCHAR(64) NOT NULL,
    resource_type VARCHAR(128) NOT NULL,
    description VARCHAR(512),
    CONSTRAINT uk_permissions_action_type UNIQUE (action, resource_type)
);

CREATE TABLE roles (
    id UUID PRIMARY KEY,
    name VARCHAR(128) NOT NULL UNIQUE,
    description VARCHAR(512),
    parent_role_id UUID,
    CONSTRAINT fk_roles_parent FOREIGN KEY (parent_role_id) REFERENCES roles(id)
);

CREATE TABLE role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES permissions(id)
);

CREATE TABLE resource_permissions (
    id UUID PRIMARY KEY,
    user_id UUID,
    role_id UUID,
    permission_id UUID NOT NULL,
    resource_id UUID,
    resource_type VARCHAR(128),
    granted_at TIMESTAMP NOT NULL,
    granted_by UUID,
    expires_at TIMESTAMP,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    grant_state VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_resperms_role FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT fk_resperms_permission FOREIGN KEY (permission_id) REFERENCES permissions(id),
    CONSTRAINT uk_resperms UNIQUE (user_id, role_id, permission_id, resource_id, resource_type)
);

CREATE TABLE approval_workflows (
    id UUID PRIMARY KEY,
    resource_id UUID NOT NULL,
    resource_type VARCHAR(128) NOT NULL,
    state VARCHAR(32) NOT NULL,
    initiator_id UUID NOT NULL,
    current_assignee_id UUID,
    description VARCHAR(2048),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE workflow_events (
    id UUID PRIMARY KEY,
    workflow_id UUID NOT NULL,
    action VARCHAR(32) NOT NULL,
    from_state VARCHAR(32),
    to_state VARCHAR(32) NOT NULL,
    actor_id UUID,
    comment VARCHAR(2048),
    occurred_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_we_workflow FOREIGN KEY (workflow_id) REFERENCES approval_workflows(id)
);
