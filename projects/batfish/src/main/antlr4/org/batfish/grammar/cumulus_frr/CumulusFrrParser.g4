parser grammar CumulusFrrParser;

import CumulusFrr_bgp, CumulusFrr_common, CumulusFrr_ip_community_list, CumulusFrr_ip_prefix_list, CumulusFrr_routemap, CumulusFrr_vrf;

options {
  superClass =
  'org.batfish.grammar.cumulus_frr.parsing.CumulusFrrBaseParser';
  tokenVocab = CumulusFrrLexer;
}

// goal rule
cumulus_frr_configuration
:
  statement+ EOF
;

// other rules
statement
:
  FRR VERSION REMARK_TEXT NEWLINE
  | FRR DEFAULTS DATACENTER NEWLINE
  | USERNAME word word NEWLINE
  | s_agentx
  | s_bgp
  | s_vrf
  | s_routemap
  | s_ip
  | SERVICE INTEGRATED_VTYSH_CONFIG NEWLINE
  | LINE VTY NEWLINE
  | LOG SYSLOG INFORMATIONAL NEWLINE
;

s_agentx
:
  AGENTX NEWLINE
;

s_ip
:
  IP
  (
    ip_community_list
    | ip_prefix_list
  )
;
