package me.rotemfo.violations.filter

import me.rotemfo.violations.Violation

/**
  * project: akka-sse
  * package: me.rotemfo.violations
  * file:    ViolationsStreamFilters
  * created: 2019-08-05
  * author:  rotem
  */
case class ViolationsStreamFilters(serviceId: Option[Long] = None,
                                   unitId: Option[String] = None,
                                   violationTypeId: Option[Int] = None,
                                   violationScopeId: Option[String] = None,
                                   severity: Option[Int] = None){

  def filter(violation: Violation): Option[Violation] = {
    var result = true
    if (serviceId.isDefined) result &= violation.getServiceid == serviceId.get
    if (unitId.isDefined && violation.getUnitid != null) {
      val split = unitId.get.split("\\|")
      result &= split.contains(violation.getUnitid)
    }
    if (violationTypeId.isDefined) result &= violation.getViolationtypeid == violationTypeId.get
    if (violationScopeId.isDefined) {
      val split = violationScopeId.get.split("\\|")
      result &= split.contains(violation.getViolationscopeid)
    }
    if (severity.isDefined) result &= violation.getSeverity == severity.get
    if (result) Some(violation) else None
  }
}
