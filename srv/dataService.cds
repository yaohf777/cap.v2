//namespace accesscertification;
using { ac } from '../db/data-model';

service DataService {
  entity DataselectionUserDetailsRolesSystemsView @readonly as projection on ac.DATASELECTION_USER_DETAILS_ROLES_SYSTEMS_VIEW;
}