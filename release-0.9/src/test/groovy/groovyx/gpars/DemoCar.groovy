//  GPars (formerly GParallelizer)
//
//  Copyright © 2008-9  The original author or authors
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License. 

package groovyx.gpars

def car = "Patriot"

def manufacturer = match(car) {
    when "Focus", "Ford"
    when "Navigator", "Lincoln"
    when "Camry", "Toyota"
    when "Civic", "Honda"
    when "Patriot", "Jeep"
    when "Jetta", "VW"
    when "Ceyene", "Porsche"
    when "Outback", "Subaru"
    when "520i", "BMW"
    when "Tundra", "Nissan"
    otherwise "Unknown"
}

println "The $car is made by $manufacturer"

def match(obj, closure) {
    closure.subject = obj
    closure.when = {value, result ->
        if (value == subject)
            throw new MatchResultException(result: result)
    }
    closure.otherwise = { return it }
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    try {
        closure()
        closure.otherwise()
    } catch (MatchResultException r) {
        r.result
    }
}

class MatchResultException extends RuntimeException {
    def result
}
