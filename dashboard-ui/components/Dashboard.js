import React from 'react'
import EventBus from 'vertx3-eventbus-client'
import Bootstrap from 'bootstrap/dist/css/bootstrap.css'

class Dashboard extends React.Component {

    constructor(props) {
        super(props);
        const eventBus = new EventBus("http://localhost:9080/eventbus");
        var _this = this;
        eventBus.enableReconnect(true);
        eventBus.onopen = function () {
            eventBus.registerHandler('dashboard.ui', function (error, message) {
                if (error === null) {
                   _this.setState(message.body);
                } else {
                    console.error(error, 'dashboard.ui');
                }
            });
        };
        this.state = {
            drivers: []
        };
    }

    render() {

        console.log(JSON.stringify(this.state.drivers));
        let rows = this.state.drivers.map(driver =>
        {
            return <TRow key={driver.name} data={driver}/>
        });    
        
        return (<table className="table table-striped">
        <THead/>
            <tbody>{rows}</tbody>
        </table>
        );

    }
}




class THead extends React.Component
{
    constructor()
    {
        super();
    }

    render()
    {
         return (<thead className="thead-default">
        <tr>
            <th>Name</th>
            <th>Occupied</th>
            <th>Passenger Count</th>
            <th>Last Updated</th>
        </tr>
        </thead>
        );
    }
}


class TRow extends React.Component
{
    constructor()
    {
        super();
    }

    render()
    {
        return (<tr>
                    <td>{this.props.data.name}</td>
                    <td>{this.props.data.occupied?"Yes":"No"}</td>
                    <td>{this.props.data.passengerCount}</td>
                    <td>{this.props.data.timestamp}</td>
                </tr>
            );
    }
}


export default Dashboard
