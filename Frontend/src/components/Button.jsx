const Button = ({ type }) => {
    const createCar = async () => {
        const response = await fetch(`http://localhost:8080/cars/create?type=${type}`, {
            method: "POST",
        });
        const data = await response.text();
        alert(data);
    };

    return <button onClick={createCar}>Create {type} Car</button>;
};

export default Button;
